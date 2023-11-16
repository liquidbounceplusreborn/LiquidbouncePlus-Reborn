#version 120

uniform vec2 u_size;
uniform float u_radius;
uniform vec4 u_color;

void main(void)
{
    gl_FragColor = vec4(u_color.rgb, u_color.a * smoothstep(1.0, 0.0, length(max((abs(gl_TexCoord[0].st - 0.5) + 0.5) * u_size - u_size + u_radius, 0.0)) - u_radius + 0.5));
}