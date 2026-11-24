#define CGLTF_IMPLEMENTATION
#include "cgltf.h"
#include <jni.h>
#include <GLES3/gl3.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <vector>
#include <cmath>
#include <algorithm>

#define LOG_TAG "WildHorizonNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static GLuint program=0, worldVao=0, worldVbo=0, playerVao=0, playerVbo=0;
static GLint mvpLoc=-1;
static int viewportW=1,viewportH=1;
static std::vector<float> worldVertices,playerVertices;
static float playerX=0,playerZ=-8,moveX=0,moveY=0,cameraYaw=0;
static bool hasPlayerModel=false;

static GLuint compileShader(GLenum type,const char* source){
 GLuint s=glCreateShader(type);glShaderSource(s,1,&source,nullptr);glCompileShader(s);
 GLint ok=0;glGetShaderiv(s,GL_COMPILE_STATUS,&ok);
 if(!ok){char log[1024];glGetShaderInfoLog(s,sizeof(log),nullptr,log);LOGE("Shader: %s",log);}return s;
}
static void tri(std::vector<float>& out,float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float r,float g,float b){
 const float v[]={ax,ay,az,r,g,b,bx,by,bz,r,g,b,cx,cy,cz,r,g,b};out.insert(out.end(),v,v+18);
}
static void quad(float x1,float z1,float x2,float z2,float y,float r,float g,float b){
 tri(worldVertices,x1,y,z1,x2,y,z1,x2,y,z2,r,g,b);
 tri(worldVertices,x1,y,z1,x2,y,z2,x1,y,z2,r,g,b);
}
static void cube(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b){
 float x0=x-sx/2,x1=x+sx/2,y0=y,y1=y+sy,z0=z-sz/2,z1=z+sz/2;
 auto face=[&](float ax,float ay,float az,float bx,float by,float bz,float cx,float cy,float cz,float dx,float dy,float dz,float shade){
  tri(worldVertices,ax,ay,az,bx,by,bz,cx,cy,cz,r*shade,g*shade,b*shade);
  tri(worldVertices,ax,ay,az,cx,cy,cz,dx,dy,dz,r*shade,g*shade,b*shade);
 };
 face(x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1,.95f);
 face(x1,y0,z0,x0,y0,z0,x0,y1,z0,x1,y1,z0,.65f);
 face(x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0,.78f);
 face(x1,y0,z1,x1,y0,z0,x1,y1,z0,x1,y1,z1,.82f);
 face(x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0,1.12f);
}
static void buildWorld(){
 worldVertices.clear();
 quad(-80,-100,80,100,-1,.24f,.34f,.20f);
 quad(-7,-100,7,100,-.94f,.16f,.17f,.18f);
 quad(-9,-100,-7,100,-.92f,.48f,.47f,.40f);
 quad(7,-100,9,100,-.92f,.48f,.47f,.40f);
 for(int i=-90;i<100;i+=12)quad(-.18f,(float)i,.18f,(float)i+5,-.90f,.88f,.82f,.56f);
 for(int i=0;i<9;i++){float x=(i%3-1)*22.f,z=-35.f+(i/3)*27.f,h=7.f+(i%4)*3.f;cube(x,h*.5f,z,10,h,10,.46f+.04f*(i%3),.48f,.52f);cube(x,h+.8f,z,10.4f,.7f,10.4f,.22f,.24f,.27f);}
}
static void upload(std::vector<float>& data,GLuint& vao,GLuint& vbo){
 glGenVertexArrays(1,&vao);glGenBuffers(1,&vbo);glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vbo);
 glBufferData(GL_ARRAY_BUFFER,data.size()*sizeof(float),data.data(),GL_STATIC_DRAW);
 glEnableVertexAttribArray(0);glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,6*sizeof(float),(void*)0);
 glEnableVertexAttribArray(1);glVertexAttribPointer(1,3,GL_FLOAT,GL_FALSE,6*sizeof(float),(void*)(3*sizeof(float)));
}
static bool loadGlb(AAssetManager* mgr,const char* path){
 AAsset* a=AAssetManager_open(mgr,path,AASSET_MODE_BUFFER);if(!a){LOGE("Model asset missing: %s",path);return false;}
 off_t n=AAsset_getLength(a);std::vector<unsigned char> bytes((size_t)n);AAsset_read(a,bytes.data(),n);AAsset_close(a);
 cgltf_options options={};cgltf_data* data=nullptr;
 cgltf_result result=cgltf_parse(&options,bytes.data(),bytes.size(),&data);
 if(result!=cgltf_result_success||!data){LOGE("GLB parse failed: %d",result);return false;}
 result=cgltf_load_buffers(&options,data,nullptr);
 if(result!=cgltf_result_success){LOGE("GLB buffers failed: %d",result);cgltf_free(data);return false;}
 float minY=1e9f,maxY=-1e9f;
 for(cgltf_size mi=0;mi<data->meshes_count;mi++)for(cgltf_size pi=0;pi<data->meshes[mi].primitives_count;pi++){
  cgltf_primitive* p=&data->meshes[mi].primitives[pi];
  for(cgltf_size ai=0;ai<p->attributes_count;ai++)if(p->attributes[ai].type==cgltf_attribute_type_position){
   cgltf_accessor* ac=p->attributes[ai].data;for(cgltf_size k=0;k<ac->count;k++){cgltf_float v[3];if(cgltf_accessor_read_float(ac,k,v,3)){minY=std::min(minY,v[1]);maxY=std::max(maxY,v[1]);}}
  }
 }
 float scale=(maxY>minY)?3.6f/(maxY-minY):1.f;
 for(cgltf_size mi=0;mi<data->meshes_count;mi++)for(cgltf_size pi=0;pi<data->meshes[mi].primitives_count;pi++){
  cgltf_primitive* p=&data->meshes[mi].primitives[pi];if(p->type!=cgltf_primitive_type_triangles)continue;
  cgltf_accessor* pos=nullptr;for(cgltf_size ai=0;ai<p->attributes_count;ai++)if(p->attributes[ai].type==cgltf_attribute_type_position)pos=p->attributes[ai].data;
  if(!pos)continue;
  cgltf_size count=p->indices?p->indices->count:pos->count;
  for(cgltf_size k=0;k+2<count;k+=3)for(int corner=0;corner<3;corner++){
   cgltf_size idx=p->indices?cgltf_accessor_read_index(p->indices,k+corner):k+corner;
   cgltf_float v[3];if(!cgltf_accessor_read_float(pos,idx,v,3))continue;
   float x=v[0]*scale,y=(v[1]-(minY+maxY)*.5f)*scale,z=v[2]*scale;
   const float col[]={.62f,.64f,.68f};
   playerVertices.insert(playerVertices.end(),{x,y,z,col[0],col[1],col[2]});
  }
 }
 cgltf_free(data);
 if(playerVertices.empty())return false;
 upload(playerVertices,playerVao,playerVbo);hasPlayerModel=true;
 LOGE("Loaded native GLB character, vertices=%zu",playerVertices.size()/6);return true;
}
static void perspective(float*m,float fovy,float aspect,float nearZ,float farZ){
 for(int i=0;i<16;i++)m[i]=0;float f=1.f/tanf(fovy*.5f);m[0]=f/aspect;m[5]=f;m[10]=(farZ+nearZ)/(nearZ-farZ);m[11]=-1;m[14]=(2*farZ*nearZ)/(nearZ-farZ);
}
static void multiply(float*out,const float*a,const float*b){for(int c=0;c<4;c++)for(int r=0;r<4;r++){out[c*4+r]=0;for(int k=0;k<4;k++)out[c*4+r]+=a[k*4+r]*b[c*4+k];}}
static void lookAt(float*m,float ex,float ey,float ez,float cx,float cy,float cz){
 float fx=cx-ex,fy=cy-ey,fz=cz-ez,fl=sqrtf(fx*fx+fy*fy+fz*fz);fx/=fl;fy/=fl;fz/=fl;
 float sx=-fz,sy=0,sz=fx,sl=sqrtf(sx*sx+sz*sz);sx/=sl;sz/=sl;
 float ux=sy*fz-sz*fy,uy=sz*fx-sx*fz,uz=sx*fy-sy*fx;
 float a[16]={sx,ux,-fx,0,sy,uy,-fy,0,sz,uz,-fz,0,0,0,0,1};
 a[12]=-(sx*ex+sz*ez);a[13]=-(uy*ey);a[14]=fx*ex+fy*ey+fz*ez;for(int i=0;i<16;i++)m[i]=a[i];
}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_initialize(JNIEnv*env,jobject,jobject assetManager){
 const char*vs="#version 300 es\nlayout(location=0) in vec3 aPos;layout(location=1) in vec3 aColor;uniform mat4 uMVP;out vec3 vColor;void main(){vColor=aColor;gl_Position=uMVP*vec4(aPos,1.0);}";
 const char*fs="#version 300 es\nprecision mediump float;in vec3 vColor;out vec4 fragColor;void main(){fragColor=vec4(vColor,1.0);}";
 GLuint v=compileShader(GL_VERTEX_SHADER,vs),f=compileShader(GL_FRAGMENT_SHADER,fs);program=glCreateProgram();glAttachShader(program,v);glAttachShader(program,f);glLinkProgram(program);glDeleteShader(v);glDeleteShader(f);mvpLoc=glGetUniformLocation(program,"uMVP");
 buildWorld();upload(worldVertices,worldVao,worldVbo);AAssetManager*mgr=AAssetManager_fromJava(env,assetManager);if(mgr)loadGlb(mgr,"models/nathan.glb");
 if(!hasPlayerModel){playerVertices.clear();cube(0,0,0,1,2,1,.1f,.3f,.8f);playerVertices=worldVertices;worldVertices.clear();}
 glEnable(GL_DEPTH_TEST);glClearColor(.48f,.68f,.82f,1);
}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_resize(JNIEnv*,jobject,jint w,jint h){viewportW=w>0?w:1;viewportH=h>0?h:1;glViewport(0,0,viewportW,viewportH);}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_setMoveInput(JNIEnv*,jobject,jfloat x,jfloat y){moveX=std::max(-1.f,std::min(1.f,x));moveY=std::max(-1.f,std::min(1.f,y));}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_setLookInput(JNIEnv*,jobject,jfloat dx){cameraYaw+=dx*.004f;}
extern "C" JNIEXPORT void JNICALL Java_com_example_NativeWorld_render(JNIEnv*,jobject){
 float dt=.016f;float forward=-moveY,side=moveX;playerX+=(side*cosf(cameraYaw)+forward*sinf(cameraYaw))*dt*5.5f;playerZ+=(side*sinf(cameraYaw)-forward*cosf(cameraYaw))*dt*5.5f;
 playerX=std::max(-75.f,std::min(75.f,playerX));playerZ=std::max(-95.f,std::min(95.f,playerZ));
 glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);float p[16],view[16],vp[16],model[16],mvp[16];perspective(p,1.0f,(float)viewportW/viewportH,.1f,260);
 float camX=playerX+sinf(cameraYaw)*10.f,camZ=playerZ+cosf(cameraYaw)*10.f;lookAt(view,camX,playerZ+6.5f,camZ,playerX,playerZ+1.4f,playerZ);
 multiply(vp,p,view);glUseProgram(program);glBindVertexArray(worldVao);glUniformMatrix4fv(mvpLoc,1,GL_FALSE,vp);glDrawArrays(GL_TRIANGLES,0,(GLsizei)(worldVertices.size()/6));
 float c=cosf(cameraYaw),s=sinf(cameraYaw);float yaw[16]={c,0,-s,0,0,1,0,0,s,0,c,0,playerX,0,playerZ,1};multiply(mvp,vp,yaw);glUniformMatrix4fv(mvpLoc,1,GL_FALSE,mvp);
 if(hasPlayerModel){glBindVertexArray(playerVao);glDrawArrays(GL_TRIANGLES,0,(GLsizei)(playerVertices.size()/6));}
}
