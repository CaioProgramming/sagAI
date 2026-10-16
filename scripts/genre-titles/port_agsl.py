"""Converts the GLSL fragment shaders of the genre titles lab into the AGSL assets of the app.

    python3 scripts/genre-titles/port_agsl.py [LAB_HTML] [OUT_DIR]

Defaults: the lab in docs/prototypes/genre-titles/genre-titles.html and app/src/main/assets.
Edit the FS_* template literals in the lab, try them in a browser, then run this script. See
docs/feature_ideas/genre_title_effects.md ("Editing a shader").
"""
import os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
LAB = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, 'docs/prototypes/genre-titles/genre-titles.html')
OUT = sys.argv[2] if len(sys.argv) > 2 else os.path.join(ROOT, 'app/src/main/assets')
src=open(LAB).read()
def tpl(name):
    # returns the template literal body for var NAME = COMMON + `...`  or COMMON = `...`
    m=re.search(r"var "+name+r" = (?:COMMON \+ )?`(.*?)`;", src, re.S)
    return m.group(1)
COMMON=tpl('COMMON')
BODIES={g:tpl('FS_'+g) for g in ['FIRE','HORROR','COWBOY','SHINOBI','CRIME','HEROES']}

# ---------- shared GLSL tweaks (emissive model: transparent background, light only) ----------
def emissive(glsl):
    g=glsl
    g=g.replace("vec3(.039, .024, .020)","vec3(0.)")
    g=g.replace("vec3(.008, .012, .04)","vec3(0.)")
    if "vec3 boxed(vec3 c, vec2 uv){" in g:
        g=g.replace("vec3 boxed(vec3 c, vec2 uv){",open(os.path.join(HERE, 'boxmask.glsl')).read().rstrip("\n"),1)
    # grain only where there is light, otherwise it would paint the whole box
    g=re.sub(r"outc \+= \(hash\(uv \* uRes \+ fract\((t|uTime)\) \* 91\.7\) - \.5\) \* ([^;]+);",
             lambda m: "outc += (hash(uv * uRes + fract(%s) * 91.7) - .5) * (%s) * smoothstep(0., .06, max(outc.r, max(outc.g, outc.b)));"%(m.group(1),m.group(2)), g)
    return g
COVER={
 'FIRE':"m0",
 'HORROR':"max(m0, drip)",
 'COWBOY':"m0",
 'SHINOBI':"max(m0, inBand)",
 'CRIME':"m0",
 'HEROES':"max(max(m0, ext * ea * (1. - m0)), ol)",
}
def with_cover(genre, g):
    ret="gl_FragColor = vec4(boxed(outc, uv), 1.);"
    assert ret in g, genre
    return g.replace(ret,"float cov = (%s) * boxMask(uv);\n  %s"%(COVER[genre], "gl_FragColor = vec4(finishRgb(boxed(outc, uv)), finishA(boxed(outc, uv), cov));"))

# ---------- emissive GLSL (for browser validation) ----------
FIN_GLSL = """
vec3 finishRgb(vec3 c){ return clamp(c, 0., 1.); }
float finishA(vec3 c, float cov){ c = clamp(c, 0., 1.); return clamp(max(max(c.r, max(c.g, c.b)), cov), 0., 1.); }
"""
def emissive_full(genre):
    com=emissive(COMMON)
    # boxed() fades to transparent
    body=with_cover(genre, emissive(BODIES[genre]))
    return com+FIN_GLSL+body

# ---------- AGSL ----------
def to_agsl(genre):
    com=emissive(COMMON)
    # drop things the app does not use in this port
    com=re.sub(r"// A CRT screen filling the layout box.*?\n}\n(?=// Simulates)","",com,flags=re.S)
    com=com.replace("precision highp float;\n","").replace("varying vec2 vUv;\n","")
    com=com.replace("uniform sampler2D uMask, uS, uCol;","uniform shader uMask;\nuniform shader uS;")
    com=com.replace("vec2 pe","float2 pe")
    body=with_cover(genre, emissive(BODIES[genre]))
    # finishers
    body=body.replace("gl_FragColor = vec4(finishRgb(boxed(outc, uv)), finishA(boxed(outc, uv), cov));","return finish(boxed(outc, uv), cov);")
    # main signature
    body=body.replace("void main(){","half4 main(float2 fragCoord){\n  float2 uv = float2(fragCoord.x / uRes.x, 1.0 - fragCoord.y / uRes.y);")
    body=re.sub(r"vec2 uv = vUv;\s*","",body)
    helpers = """
// Child shaders are sampled in pixel space; the lab sampled normalised uv with y up.
half4 sM(float2 p) { return uMask.eval(float2(p.x * uRes.x, (1.0 - p.y) * uRes.y)); }
half4 sS(float2 p) { return uS.eval(float2(p.x * uRes.x, (1.0 - p.y) * uRes.y)); }
// Emissive output: the effect is light on a transparent layer. A pixel is as opaque as it is bright,
// or as covered by the letters, so nothing paints a backdrop and gradientFill/shimmer find no box.
half4 finish(float3 c, float cov) {
    c = clamp(c, 0.0, 1.0);
    float a = clamp(max(max(c.r, max(c.g, c.b)), cov), 0.0, 1.0);
    return half4(half3(c), half(a));
}
"""
    full=com+helpers+body
    full=re.sub(r"texture2D\(uMask,","sM(",full)
    # vecN -> floatN first so the comma split below sees the final type names
    full=re.sub(r"\bvec([234])\b",r"float\1",full)
    def split_uniforms(m):
        typ=m.group(1); names=[n.strip() for n in m.group(2).split(',')]
        return "\n".join("uniform %s %s;"%(typ,n) for n in names)
    full=re.sub(r"uniform (float[234]?) ([A-Za-z0-9_]+(?:, [A-Za-z0-9_]+)+);",split_uniforms,full)
    full=re.sub(r"texture2D\(uS,","sS(",full)
    full=re.sub(r"\bvec([234])\b",r"float\1",full)
    full=full.replace("uniform float3 uC1, uC2, uC3, uC4, uHalo, uText, uOpt;","uniform float3 uC1, uC2, uC3, uC4, uHalo, uText, uOpt;")
    return full
# lab shader name -> genre asset name
ASSETS={'FIRE':'fantasy','HORROR':'horror','COWBOY':'cowboy','SHINOBI':'shinobi','CRIME':'crime','HEROES':'heroes'}
if __name__=='__main__':
    os.makedirs(OUT,exist_ok=True)
    for g in BODIES:
        path=os.path.join(OUT,'genre_title_%s.agsl'%ASSETS[g])
        open(path,'w').write(to_agsl(g))
        print('wrote',path)
