#version 410

in vec3 v_normal;	// world space - interpolated from vertex shader
in vec3 FragPos;

uniform vec4 u_lightDir; 		// in world coordinates
uniform vec4 u_diffuseMaterial;	// RGBA

layout(location = 0) out vec4 colour;

void main() {
    // make sure the lightDir and normal have length 1
    vec3 normal = normalize(v_normal);
    vec3 lightDir = normalize(u_lightDir.xyz- FragPos);
    
    // Lambert diffuse lighting equation (assuming the light is white)
    vec4 diffuse = u_diffuseMaterial * max(0, dot(lightDir, normal));

    colour = diffuse;
}

