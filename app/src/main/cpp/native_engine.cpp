#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <vector>
#include <cmath>

#define LOG_TAG "WildHorizonNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static GLuint program = 0;
static GLuint vao = 0;
static GLuint vbo = 0;
static GLint mvpLoc = -1;
static GLint posLoc = 0;
static GLint colorLoc = 1;
static int viewportW = 1, viewportH = 1;
static float angle = 0.0f;
static std::vector<float> vertices;

static GLuint compileShader(GLenum type, const char* source) {
    GLuint shader = glCreateShader(type);
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    GLint ok = 0; glGetShaderiv(shader, GL_COMPILE_STATUS, &ok);
    if (!ok) { char log[1024]; glGetShaderInfoLog(shader, sizeof(log), nullptr, log); LOGE("Shader: %s", log); }
    return shader;
}
static void addTri(float ax,float ay,float az,float ar,float ag,float ab,
                   float bx,float by,float bz,float br,float bg,float bb,
                   float cx,float cy,float cz,float cr,float cg,float cb) {
    const float v[] = {ax,ay,az,ar,ag,ab,bx,by,bz,br,bg,bb,cx,cy,cz,cr,cg,cb};
    vertices.insert(vertices.end(), v, v+18);
}
static void quad(float x1,float z1,float x2,float z2,float y,float r,float g,float b) {
    addTri(x1,y,z1,r,g,b,x2,y,z1,r,g,b,x2,y,z2,r,g,b);
    addTri(x1,y,z1,r,g,b,x2,y,z2,r,g,b,x1,y,z2,r,g,b);
}
static void cube(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b) {
    float x0=x-sx/2,x1=x+sx/2,y0=y,y1=y+sy,z0=z-sz/2,z1=z+sz/2;
    auto face=[&](float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float shade){
        addTri(ax,ay,az,r*shade,g*shade,b*shade,bx,by,bz,r*shade,g*shade,b*shade,cx,cy,cz,r*shade,g*shade,b*shade);
        addTri(ax,ay,az,r*shade,g*shade,b*shade,cx,cy,cz,r*shade,g*shade,b*shade,dx,dy,dz,r*shade,g*shade,b*shade);
    };
    face(x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1,0.95f);
    face(x1,y0,z0,x0,y0,z0,x0,y1,z0,x1,y1,z0,0.65f);
    face(x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0,0.78f);
    face(x1,y0,z1,x1,y0,z0,x1,y1,z0,x1,y1,z1,0.82f);
    face(x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0,1.12f);
}
static void buildWorld() {
    vertices.clear();
    quad(-80,-100,80,100,-1.0f,0.24f,0.34f,0.20f);
    quad(-7,-100,7,100,-0.94f,0.16f,0.17f,0.18f);
    quad(-9,-100,-7,100,-0.92f,0.48f,0.47f,0.40f);
    quad(7,-100,9,100,-0.92f,0.48f,0.47f,0.40f);
    for(int i=-90;i<100;i+=12) {
        quad(-0.18f,(float)i,0.18f,(float)i+5,-0.90f,0.88f,0.82f,0.56f);
    }
    for(int i=0;i<9;i++) {
        float x=(i%3-1)*22.0f, z=-35.0f+(i/3)*27.0f;
        float h=7.0f+(i%4)*3.0f;
        cube(x,h*0.5f,z,10.0f,h,10.0f,0.46f+0.04f*(i%3),0.48f,0.52f);
        cube(x,h+0.8f,z,10.4f,0.7f,10.4f,0.22f,0.24f,0.27f);
    }
    cube(0,1.2f,-8,1.4f,2.4f,0.9f,0.12f,0.34f,0.78f);
}
static void perspective(float* m,float fovy,float aspect,float nearZ,float farZ) {
    for(int i=0;i<16;i++) m[i]=0;
    float f=1.0f/tanf(fovy*0.5f);
    m[0]=f/aspect; m[5]=f; m[10]=(farZ+nearZ)/(nearZ-farZ);
    m[11]=-1.0f; m[14]=(2.0f*farZ*nearZ)/(nearZ-farZ);
}
static void multiply(float* out,const float* a,const float* b) {
    for(int c=0;c<4;c++) for(int r=0;r<4;r++) {
        out[c*4+r]=0;
        for(int k=0;k<4;k++) out[c*4+r]+=a[k*4+r]*b[c*4+k];
    }
}
static void lookAt(float* m,float ex,float ey,float ez,float cx,float cy,float cz) {
    float fx=cx-ex,fy=cy-ey,fz=cz-ez, fl=sqrtf(fx*fx+fy*fy+fz*fz); fx/=fl;fy/=fl;fz/=fl;
    float sx=-fz,sy=0,sz=fx, sl=sqrtf(sx*sx+sz*sz); sx/=sl;sz/=sl;
    float ux=sy*fz-sz*fy,uy=sz*fx-sx*fz,uz=sx*fy-sy*fx;
    float a[16]={sx,ux,-fx,0, sy,uy,-fy,0, sz,uz,-fz,0, 0,0,0,1};
    a[12]=-(sx*ex+sy*ey+sz*ez); a[13]=-(ux*ex+uy*ey+uz*ez); a[14]=(fx*ex+fy*ey+fz*ez);
    for(int i=0;i<16;i++) m[i]=a[i];
}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_initialize(JNIEnv*,jobject) {
    const char* vs="#version 300 es\nlayout(location=0) in vec3 aPos; layout(location=1) in vec3 aColor; uniform mat4 uMVP; out vec3 vColor; void main(){vColor=aColor;gl_Position=uMVP*vec4(aPos,1.0);}";
    const char* fs="#version 300 es\nprecision mediump float; in vec3 vColor; out vec4 fragColor; void main(){fragColor=vec4(vColor,1.0);}";
    GLuint v=compileShader(GL_VERTEX_SHADER,vs), f=compileShader(GL_FRAGMENT_SHADER,fs);
    program=glCreateProgram(); glAttachShader(program,v);glAttachShader(program,f);glLinkProgram(program);
    glDeleteShader(v);glDeleteShader(f);
    mvpLoc=glGetUniformLocation(program,"uMVP");
    glGenVertexArrays(1,&vao);glGenBuffers(1,&vbo);
    buildWorld();
    glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
    glBufferData(GL_ARRAY_BUFFER,vertices.size()*sizeof(float),vertices.data(),GL_STATIC_DRAW);
    glEnableVertexAttribArray(posLoc);glVertexAttribPointer(posLoc,3,GL_FLOAT,GL_FALSE,6*sizeof(float),(void*)0);
    glEnableVertexAttribArray(colorLoc);glVertexAttribPointer(colorLoc,3,GL_FLOAT,GL_FALSE,6*sizeof(float),(void*)(3*sizeof(float)));
    glEnable(GL_DEPTH_TEST);glClearColor(0.48f,0.68f,0.82f,1.0f);
}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_resize(JNIEnv*,jobject,jint w,jint h) {
    viewportW=w>0?w:1;viewportH=h>0?h:1;glViewport(0,0,viewportW,viewportH);
}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_render(JNIEnv*,jobject) {
    glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    float p[16],v[16],mvp[16];
    perspective(p,0.92f,(float)viewportW/(float)viewportH,0.1f,240.0f);
    angle+=0.0008f;
    lookAt(v,sinf(angle)*1.5f,13.0f,25.0f,0.0f,2.0f,-18.0f);
    multiply(mvp,p,v);
    glUseProgram(program);glUniformMatrix4fv(mvpLoc,1,GL_FALSE,mvp);
    glBindVertexArray(vao);glDrawArrays(GL_TRIANGLES,0,(GLsizei)(vertices.size()/6));
}
