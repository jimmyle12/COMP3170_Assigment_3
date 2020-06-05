#version 410

in vec3 v_normal;	// world space - interpolated from vertex shader
in vec3 FragPos;

uniform vec4 u_lightDir; 		// in world coordinates
uniform vec4 u_viewDir; 		// in world coordinates
uniform vec4 u_specularMaterial;	// RGBA
uniform float u_specularity;	// Phong coefficient 

layout(location = 0) out vec4 colour;

vec4 phongModel(vec3 normal, vec3 lightDir) {
    // make sure the viewDir has length 1
    vec3 viewDir = normalize(u_viewDir.xyz);    
	// calculate the reflection vector
	vec3 reflected = reflect(lightDir, normal);  
    
    // Phong model specular lighting equation (assuming the light is white)
    vec3 rgb = vec3(u_specularMaterial);
    vec3 result = rgb * pow(max(0,dot(reflected, viewDir)), u_specularity);
    vec4 result2 = vec4(rgb, u_specularMaterial.w);
    return result2;
}

void main() {
    // make sure the lightDir and normal have length 1
    vec3 normal = normalize(v_normal);
    vec3 lightDir = normalize(u_lightDir.xyz - FragPos);
    
    vec4 specular = vec4(0);
    // only apply specular lighting if the light is in front of the surface 
    if (dot(lightDir, normal) > 0) {
    	specular = phongModel(normal, lightDir);
	}

    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * vec3(1,1,1);

    colour = specular + vec4(ambient * vec3(u_specularMaterial), 0);

   	// interpolate to fragment colour
   	colour = specular;
}
