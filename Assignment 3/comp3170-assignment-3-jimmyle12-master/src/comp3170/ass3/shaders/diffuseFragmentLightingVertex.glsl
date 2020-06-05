#version 410

in vec3 a_position;	// model coordinates	
in vec3 a_normal;	// model coordinates
out vec3 FragPos;

uniform mat4 u_mvpMatrix;		// model -> NDC
uniform mat3 u_normalMatrix;	// model normal -> world
uniform mat4 u_worldMatrix;

out vec3 v_normal;	// world coordinates

void main() {
    gl_Position = u_mvpMatrix * vec4(a_position, 1);

    FragPos = vec3(u_worldMatrix * vec4 (a_position, 1.0));
    
    // convert the normal to world coordinates
    vec3 normal = u_normalMatrix * a_normal;

    // interpolate to fragment normal
    v_normal = normal;
}

