#version 460 core

in vec2 texCoords;

out vec4 FragColor;

void main()
{
    FragColor = vec4(texCoords.x, 0.0f, 0.0f, 1.0f);
}