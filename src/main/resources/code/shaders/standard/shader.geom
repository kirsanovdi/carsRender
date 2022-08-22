#version 460 core

layout (triangles) in;
layout (triangle_strip, max_vertices = 3) out;

uniform mat4 camMatrix;
//uniform vec3 camPos;

out vec2 texCoords;

in DATA
{
    vec2 texCoords;
} data_in[];

//нормаль vec4 к плоскости (нормированная)
vec4 getNormal(vec4 v1, vec4 v2, vec4 v3){
    vec4 vv12 = v2 - v1;
    vec4 vv13 = v3 - v1;
    return vec4(normalize(cross(vec3(vv12),vec3(vv13))), 0.00f);
}

vec4 getCenter(vec4 v1, vec4 v2, vec4 v3){
    return (v1 + v2 + v3)/3.00f;
}

void main(){
    vec4 v1 = gl_in[0].gl_Position, v2 = gl_in[1].gl_Position, v3 = gl_in[2].gl_Position;
    vec4 sn = getNormal(v1, v2, v3) * 0.00f;
    vec4 center = getCenter(v1, v2, v3);
    float sk = 0.00f;
    vec4 s1 = normalize(v1 - center)*sk, s2 = normalize(v2 - center)*sk, s3 = normalize(v3 - center)*sk;

    gl_Position = camMatrix * (v1 + sn + s1);
    texCoords = data_in[0].texCoords;
    EmitVertex();

    gl_Position = camMatrix * (v2 + sn + s2);
    texCoords = data_in[1].texCoords;
    EmitVertex();

    gl_Position = camMatrix * (v3 + sn + s3);
    texCoords = data_in[2].texCoords;
    EmitVertex();

    EndPrimitive();
}