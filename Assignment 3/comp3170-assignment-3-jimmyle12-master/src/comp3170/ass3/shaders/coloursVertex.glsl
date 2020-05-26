#version 410

// Vertex colouring vertex shader

in vec3 a_position;		// MODEL
in vec4 a_colour;		// RGBA

out vec4 v_colour;		// RGBA

uniform mat4 u_mvpMatrix;	// MODEL -> NDC

void main() {
    gl_Position = u_mvpMatrix * vec4(a_position,1);
    v_colour = a_colour;
}

