package com.ilustris.sagai.di

import com.ilustris.sagai.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Inet4Address
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * IPv4 first, otherwise DNS order as given.
 *
 * OkHttp tries addresses one at a time, each against the full `connectTimeout` — there is no
 * racing between families. A network that advertises IPv6 (DHCP/RA hands out an address, the
 * network still gets marked validated) but silently drops outbound IPv6 packets — no RST, no ICMP
 * unreachable, just nothing — leaves a request stuck for that entire timeout on every IPv6 address
 * DNS returned before it ever tries the IPv4 one that would have worked immediately. Seen from the
 * app as a request that logs its own start and then never resolves — success, failure, or
 * timeout — for however many IPv6 addresses come first.
 */
private class Ipv4FirstDns(
    private val delegate: Dns = Dns.SYSTEM,
) : Dns {
    override fun lookup(hostname: String): List<java.net.InetAddress> =
        delegate.lookup(hostname).sortedBy { it !is Inet4Address }
}

/** OkHttp client for binary downloads (fonts, audio) — no HTTP logging. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val AUDIO_TIMEOUT_SECONDS = 120L

    // Establishing a TCP connection either succeeds within a few seconds or it isn't going to —
    // unlike readTimeout, there is no legitimate slow-generation reason to give it AUDIO_TIMEOUT_SECONDS.
    // Kept short specifically so a dead-on-arrival address (see Ipv4FirstDns) fails fast per attempt.
    private const val CONNECT_TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    @DownloadOkHttpClient
    fun provideDownloadOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .dns(Ipv4FirstDns())
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder =
            OkHttpClient
                .Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(AUDIO_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .dns(Ipv4FirstDns())
                // Without this, a connection that died silently (wifi handoff, a NAT box
                // dropping it, the radio waking from sleep) can sit in the pool looking alive:
                // OkHttp reuses it, the write buffers locally and "succeeds", and the read then
                // blocks with no data and no socket-level error ever arriving to trip
                // readTimeout — seen as a request that just never returns. An HTTP/2 ping every
                // 15s forces OkHttp to notice and fail that connection instead of waiting on it.
                .pingInterval(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor { chain ->
                val request = chain.request()
                val isStreaming = request.url.encodedPath.contains("streamGenerateContent")
                HttpLoggingInterceptor().apply {
                    level =
                        if (isStreaming) {
                            HttpLoggingInterceptor.Level.HEADERS
                        } else {
                            HttpLoggingInterceptor.Level.BODY
                        }
                    redactHeader("x-goog-api-key")
                }.intercept(chain)
            }
        }

        return builder.build()
    }
}
