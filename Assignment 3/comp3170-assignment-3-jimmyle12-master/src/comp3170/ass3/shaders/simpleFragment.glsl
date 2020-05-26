#version 410

// simple fragment shader

uniform vec4 u_colour;		// RGBA

layout(location = 0) out vec4 colour;	// RGBA

void main() {	
	colour = u_colour;
}

