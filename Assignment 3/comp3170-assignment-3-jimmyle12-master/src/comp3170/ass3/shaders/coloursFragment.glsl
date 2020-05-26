#version 410

// vertex colouring fragment shader

in vec4 v_colour;	// RGBA

layout(location = 0) out vec4 colour;	// RGBA

void main() {
    colour = v_colour;
}

