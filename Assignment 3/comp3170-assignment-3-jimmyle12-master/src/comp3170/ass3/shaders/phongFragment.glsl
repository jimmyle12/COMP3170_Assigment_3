#version 410

in vec4 gl_FragCoord ;

in vec3 v_normal;	// world space - interpolated from vertex shader
in vec3 FragPos;

uniform vec4 u_lightDir; 		// in world coordinates
uniform vec4 u_viewDir; 		// in world coordinates
uniform vec4 u_specularMaterial;	// RGBA
//uniform float u_specularity;	// Phong coefficient

layout(location = 0) out vec4 colour;

float specularStrength = 2;
float shininess = 32;
vec3 lightColour = vec3(1, 1, 1);

vec3 phongModel(vec3 normal, vec3 lightDir) {
    // make sure the viewDir has length 1
    vec3 viewDir = normalize(u_viewDir.xyz);    
	// calculate the reflection vector
	vec3 reflected = reflect(lightDir, normal);  
    
    // Phong model specular lighting equation (assuming the light is white)
    vec3 result = lightColour * pow(max(0,dot(reflected, viewDir)), shininess) * specularStrength ;
    return result;
}

void main() {
    // make sure the lightDir and normal have length 1
    vec3 normal = normalize(v_normal);
    float freq = 20;
    float x = FragPos.x * freq;
    float d = sqrt(1+pow(cos(x),2)/64);
    normal.x =  (-cos(x)/8)/d;
    normal.y =  1/d;
    normal.z = 0;
    normal = normalize(normal);

    vec3 lightDir = normalize(u_lightDir.xyz - FragPos);
    
    vec3 specular = vec3(0);
    // only apply specular lighting if the light is in front of the surface 
//    if (dot(lightDir, normal) > 0) {
    	specular = phongModel(normal, lightDir);
//	}

    //calculating ambient
    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * vec3(1,1,1);

    //calculating diffuse
    vec3 diffuse = lightColour * max(0, dot(lightDir, normal));

//    vec3 rgb = specular +  (ambient + diffuse) * vec3(u_specularMaterial);
    vec3 rgb = specular + (diffuse + ambient ) * vec3(u_specularMaterial);

   	// preserving alpha
    float c = (normal.z )/2;
   	colour = vec4(rgb , u_specularMaterial.w);
}
