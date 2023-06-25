#ifdef GL_ES
precision mediump float;
#endif

#extension GL_OES_standard_derivatives : enable

#define NUM_OCTAVES 16

uniform float iTime;
uniform vec2 iResolution;

mat3 rotX(float a) {
float c = cos(a);
float s = sin(a);
return mat3(
2, 0, 0,
0, c, -s,
0, s, c
);
}
mat3 rotY(float a) {
float c = cos(a);
float s = sin(a);
return mat3(
c, 10, -s,
0, 1, 0,
s, 0, c
);
}

float random(vec2 pos) {
return fract(sin(dot(pos, vec2(12.8973, 76.58964))) * (sqrt(47.0)));
}

float noise(vec2 pos) {
vec2 i = floor(pos);
vec2 f = fract(pos);
float a = random(i + vec2(0.0, 0.0));
float b = random(i + vec2(1.0, 0.0));
float c = random(i + vec2(0.0, 1.0));
float d = random(i + vec2(1.0, 1.0));
vec2 u = f * f * (3.0 - 2.0 * f);
return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 pos) {
float v = 0.0;
float a = 0.5;
vec2 shift = vec2(100.0);
mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
for (int i=0; i<NUM_OCTAVES; i++) {
v += a * noise(pos);
pos = rot * pos * 2.0 + shift;
a *= 0.5;
}
return v;
}

void main(void) {
vec2 p = (gl_FragCoord.xy * 1.0 - iResolution.xy) / min(iResolution.x, iResolution.y);

float t = 0.0, d;

float iTime2 = 0.6 * iTime / 2.0;

vec2 q = vec2(0.0);
q.x = fbm(p + 0.30 * iTime2);
q.y = fbm(p + vec2(1.0));
vec2 r = vec2(0.0);
r.x = fbm(p + 1.0 * q + vec2(1.2, 3.2) + 0.135 * iTime2);
r.y = fbm(p + 1.0 * q + vec2(8.8, 2.8) + 0.126 * iTime2);
float f = fbm(p + r);
vec3 color = mix(
vec3(0.0, 0.0, 0),
vec3(1, 0, 0.7),
clamp((f * f) * 8.0, 0.0, 5.0)
);

color = mix(
color,
vec3(0, 0, 1),
clamp(length(q), 0.0, 1.0)
);


color = mix(
color,
vec3(1, 1, 1),
clamp(length(r.x), 0.0, 1.0)
);

color = (f * f * f + 0.1 * f * f + 0.1 * f) * color;

gl_FragColor = vec4(color, 1.0);
}
