#version 330 core

layout (location = 0) in vec2 inPos;
layout (location = 1) in vec2 inTexCoords;

out DATA
{
    vec2 texCoords;
} data_out;

void main()
{
    data_out.texCoords = inTexCoords;
    gl_Position = vec4(inPos.x, inPos.y, 0.0, 1.0);
}