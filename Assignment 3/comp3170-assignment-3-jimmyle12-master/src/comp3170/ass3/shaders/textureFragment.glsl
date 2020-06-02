#version 410


in vec3 v_normal;	// world space - interpolated from vertex shader

uniform vec4 u_lightDir; 		// in world coordinates

//uniform vec4 u_colour;
uniform sampler2D u_diffuseTexture;
in vec2 v_texcoord;

layout(location = 0) out vec4 colour;

const float GAMMA = 2.722;

void main() {
    // make sure the lightDir and normal have length 1
    vec3 normal = normalize(v_normal);
    vec3 lightDir = normalize(u_lightDir.xyz);

    vec4 dColor = texture(u_diffuseTexture, v_texcoord);
    dColor = pow(dColor, vec4(GAMMA));

    vec4 diffuse = dColor * max(0, dot(lightDir, normal));

    colour = pow(diffuse, vec4(1.0/GAMMA));
}

