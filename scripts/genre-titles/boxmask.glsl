float boxMask(vec2 uv){
  if (uBoxCfg.x < .5) return 1.;
  vec2 px = uv * uRes, lo = uBox.xy * uRes, hi = uBox.zw * uRes;
  float soft = uBoxCfg.y;
  float fx = max(soft > .5 ? uFw.x : 1., 1.), ft = max(soft > .5 ? uFw.y : 1., 1.), fb = max(soft > .5 ? uFw.z : 1., 1.);
  float m = smoothstep(lo.x, lo.x + fx, px.x) * (1. - smoothstep(hi.x - fx, hi.x, px.x));
  return m * smoothstep(lo.y, lo.y + fb, px.y) * (1. - smoothstep(hi.y - ft, hi.y, px.y));
}
vec3 boxed(vec3 c, vec2 uv){
