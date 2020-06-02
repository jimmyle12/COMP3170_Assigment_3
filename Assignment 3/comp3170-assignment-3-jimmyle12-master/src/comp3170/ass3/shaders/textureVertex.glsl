#version 410

in vec3 a_position;		// MODEL
in vec2 a_texcoord;		// UV
out vec2 v_texcoord;	// UV

in vec3 a_normal;	// model coordinates
uniform mat3 u_normalMatrix;	// model normal -> world

uniform mat4 u_mvpMatrix;	// MODEL -> NDC

out vec3 v_normal;	// world coordinates

void main() {
    gl_Position = u_mvpMatrix * vec4(a_position,1);
    v_texcoord = a_texcoord;

    // convert the normal to world coordinates
    vec3 normal = u_normalMatrix * a_normal;

    // interpolate to fragment normal
    v_normal = normal;
}

