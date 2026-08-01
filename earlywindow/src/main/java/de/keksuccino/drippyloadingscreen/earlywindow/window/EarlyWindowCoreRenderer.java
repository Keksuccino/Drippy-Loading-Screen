package de.keksuccino.drippyloadingscreen.earlywindow.window;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Renders the early window without removed fixed-function APIs. Keep this renderer core-profile compatible because
 * macOS does not provide the OpenGL compatibility profile requested by older Drippy versions.
 */
final class EarlyWindowCoreRenderer implements AutoCloseable {

    private static final int FLOATS_PER_VERTEX = 4;
    private static final int VERTEX_STRIDE_BYTES = FLOATS_PER_VERTEX * Float.BYTES;
    private static final String VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec2 Position;
            layout (location = 1) in vec2 TexCoord;
            uniform vec2 ScreenSize;
            out vec2 fragmentTexCoord;

            void main() {
                vec2 ndc = (Position / ScreenSize) * 2.0 - 1.0;
                gl_Position = vec4(ndc.x, -ndc.y, 0.0, 1.0);
                fragmentTexCoord = TexCoord;
            }
            """;
    private static final String FRAGMENT_SHADER = """
            #version 330 core
            in vec2 fragmentTexCoord;
            out vec4 FragmentColor;
            uniform vec4 Color;
            uniform sampler2D TextureSampler;
            uniform int UseTexture;

            void main() {
                vec4 baseColor = UseTexture == 1 ? texture(TextureSampler, fragmentTexCoord) : vec4(1.0);
                FragmentColor = baseColor * Color;
            }
            """;
    private static final int[] TEXT_QUAD_TRIANGLES = {0, 1, 2, 2, 3, 0};

    private final int shaderProgram;
    private final int vertexArray;
    private final int vertexBuffer;
    private final int screenSizeUniform;
    private final int colorUniform;
    private final int useTextureUniform;
    private final int textureSamplerUniform;
    private FloatBuffer uploadBuffer = BufferUtils.createFloatBuffer(256);

    EarlyWindowCoreRenderer() {
        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        this.shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(this.shaderProgram, vertexShader);
        GL20.glAttachShader(this.shaderProgram, fragmentShader);
        GL20.glLinkProgram(this.shaderProgram);
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        if (GL20.glGetProgrami(this.shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            String infoLog = GL20.glGetProgramInfoLog(this.shaderProgram);
            GL20.glDeleteProgram(this.shaderProgram);
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Failed to link early-window shader: " + infoLog);
        }

        this.screenSizeUniform = GL20.glGetUniformLocation(this.shaderProgram, "ScreenSize");
        this.colorUniform = GL20.glGetUniformLocation(this.shaderProgram, "Color");
        this.useTextureUniform = GL20.glGetUniformLocation(this.shaderProgram, "UseTexture");
        this.textureSamplerUniform = GL20.glGetUniformLocation(this.shaderProgram, "TextureSampler");

        this.vertexArray = GL30.glGenVertexArrays();
        this.vertexBuffer = GL15.glGenBuffers();
        GL30.glBindVertexArray(this.vertexArray);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE_BYTES, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, VERTEX_STRIDE_BYTES, 2L * Float.BYTES);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    void beginFrame(int screenWidth, int screenHeight) {
        GL20.glUseProgram(this.shaderProgram);
        GL30.glBindVertexArray(this.vertexArray);
        GL20.glUniform2f(this.screenSizeUniform, Math.max(1.0f, screenWidth), Math.max(1.0f, screenHeight));
        GL20.glUniform1i(this.textureSamplerUniform, 0);
    }

    void endFrame() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
    }

    void drawSolidQuad(float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        drawVertices(GL11.GL_TRIANGLE_STRIP, quadVertices(x, y, width, height, 0.0f, 0.0f, 0.0f, 0.0f), 4, 0, red, green, blue, alpha);
    }

    void drawRectangleOutline(float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        float[] vertices = {
                x, y, 0.0f, 0.0f,
                x + width, y, 0.0f, 0.0f,
                x + width, y + height, 0.0f, 0.0f,
                x, y + height, 0.0f, 0.0f
        };
        drawVertices(GL11.GL_LINE_LOOP, vertices, 4, 0, red, green, blue, alpha);
    }

    void drawTexturedQuad(int textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        drawVertices(GL11.GL_TRIANGLE_STRIP, quadVertices(x, y, width, height, u0, v0, u1, v1), 4, 1, 1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    void drawText(ByteBuffer sourceVertices, int quadCount, float x, float y, float red, float green, float blue, float alpha, float scale) {
        if (quadCount <= 0) {
            return;
        }
        int vertexCount = quadCount * TEXT_QUAD_TRIANGLES.length;
        FloatBuffer vertices = prepareUploadBuffer(vertexCount * FLOATS_PER_VERTEX);
        for (int quad = 0; quad < quadCount; quad++) {
            int quadVertexBase = quad * 4;
            for (int index : TEXT_QUAD_TRIANGLES) {
                int sourceOffset = (quadVertexBase + index) * 16;
                vertices.put(x + sourceVertices.getFloat(sourceOffset) * scale);
                vertices.put(y + sourceVertices.getFloat(sourceOffset + Float.BYTES) * scale);
                vertices.put(0.0f);
                vertices.put(0.0f);
            }
        }
        vertices.flip();
        drawBuffer(GL11.GL_TRIANGLES, vertices, vertexCount, 0, red, green, blue, alpha);
    }

    private static float[] quadVertices(float x, float y, float width, float height, float u0, float v0, float u1, float v1) {
        return new float[]{
                x, y, u0, v0,
                x + width, y, u1, v0,
                x, y + height, u0, v1,
                x + width, y + height, u1, v1
        };
    }

    private void drawVertices(int mode, float[] vertices, int vertexCount, int useTexture, float red, float green, float blue, float alpha) {
        FloatBuffer buffer = prepareUploadBuffer(vertices.length);
        buffer.put(vertices).flip();
        drawBuffer(mode, buffer, vertexCount, useTexture, red, green, blue, alpha);
    }

    private void drawBuffer(int mode, FloatBuffer vertices, int vertexCount, int useTexture, float red, float green, float blue, float alpha) {
        GL20.glUniform4f(this.colorUniform, red, green, blue, alpha);
        GL20.glUniform1i(this.useTextureUniform, useTexture);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STREAM_DRAW);
        GL11.glDrawArrays(mode, 0, vertexCount);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private FloatBuffer prepareUploadBuffer(int floatCount) {
        if (this.uploadBuffer.capacity() < floatCount) {
            int capacity = this.uploadBuffer.capacity();
            while (capacity < floatCount) {
                capacity *= 2;
            }
            this.uploadBuffer = BufferUtils.createFloatBuffer(capacity);
        }
        this.uploadBuffer.clear();
        return this.uploadBuffer;
    }

    private static int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String infoLog = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("[DRIPPY LOADING SCREEN] Failed to compile early-window shader: " + infoLog);
        }
        return shader;
    }

    @Override
    public void close() {
        GL15.glDeleteBuffers(this.vertexBuffer);
        GL30.glDeleteVertexArrays(this.vertexArray);
        GL20.glDeleteProgram(this.shaderProgram);
    }
}
