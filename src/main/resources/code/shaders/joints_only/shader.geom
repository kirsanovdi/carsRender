#version 460 core

layout (lines) in;
layout (line_strip, max_vertices = 2) out;

uniform mat4 camMatrix;

out vec2 texCoords;

in DATA
{
    vec2 texCoords;
} data_in[];

void main(){
    vec4 v1 = gl_in[0].gl_Position, v2 = gl_in[1].gl_Position;

    gl_Position = camMatrix * v1;
    texCoords = data_in[0].texCoords;
    EmitVertex();

    gl_Position = camMatrix * v2;
    texCoords = data_in[1].texCoords;
    EmitVertex();

    EndPrimitive();
}