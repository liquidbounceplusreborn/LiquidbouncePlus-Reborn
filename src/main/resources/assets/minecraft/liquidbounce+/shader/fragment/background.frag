uniform float iTime;
uniform vec2 iResolution;

#define S sin
#define C cos
#define t iTime
#define X uv.x*32.
#define Y -uv.y*32.

void main( void ) {
	vec2 uv = ( gl_FragCoord.xy-.5* iResolution.xy )/iResolution.y-.5 ;
	float t = iTime * 0.1;
	
	float c = S(X/10.+Y/15.)*C(X/20.+t+cos(.05*t+Y/5.));
	vec3 a_color = vec3(.8, .8, .8) + c;
	vec3 b_color = vec3(.8, .8, .8);
	vec3 color = mix(a_color, b_color, 0.7);
	gl_FragColor = vec4((floor(color * 4.0) / 30.0), 1.0 );
}