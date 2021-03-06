/*

 A tool for visualisation
 and analysis of biological networks

 Copyright (c) 2006-2012 Genome Research Ltd.
 Authors: Thanos Theo, Anton Enright, Leon Goldovsky, Ildefonso Cases, Markus Brosch, Stijn van Dongen, Michael Kargas, Benjamin Boyer and Tom Freeman


 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

 @ author, GLSL & OpenGL code author Thanos Theo, 2009-2010-2011-2012

*/

FS_VARYING vec3 FS_POSITION;
FS_VARYING vec3 FS_MC_POSITION;
FS_VARYING vec3 FS_NORMAL;
FS_VARYING vec4 FS_SCENE_COLOR;
FS_VARYING vec2 FS_TEX_COORD;
FS_VARYING float FS_V;

uniform sampler3D woodPerlinNoise3DTexture;

uniform bool woodFog;
uniform bool woodTexturing;
uniform bool woodState;
uniform float woodTimer;
uniform bool woodOldLCDStyleTransparency;
uniform bool woodErosion;
uniform bool woodSolidWireFrame;

const float PI = 3.14159;
const float intensityLevel = 1.0;
const float intensityTransparencyLevel = 0.8 * intensityLevel;
const float extraLightIntensityFactor = 0.35;

const vec4  LightWoodColor = vec4(0.6, 0.3, 0.1, 1.0);
const vec4  DarkWoodColor = vec4(0.4, 0.2, 0.07, 1.0);
const float RingFrequency = 4.0;
const float LightGrains = 1.0;
const float DarkGrains = 0.0;
const float GrainThreshold = 10.5;
const vec3  NoiseScale = vec3(0.5, 0.1, 0.1);
const float Noisiness = 2.0;
const float GrainScale = 40.0;

// animation related GPU Computing variables
uniform bool AnimationGPUComputingMode;
uniform bool ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION;


void applyErosion(in vec4);

void applyOldStyleTransparency();
vec4 applyAnimationGPUComputing(in vec4);
vec4 applyADSLightingModel(in bool, in bool, in vec3, in vec3, in vec4);
void applyTexture(inout vec4, in vec2);
vec4 applyFog(in vec4);


void main()
{
    if (woodOldLCDStyleTransparency)
        applyOldStyleTransparency();

    vec3 MCpositionDistorted = NoiseScale * FS_MC_POSITION;
    MCpositionDistorted.x += sin(woodTimer);
    MCpositionDistorted.y += sin(woodTimer + PI / 2.0);
    MCpositionDistorted.z += sin(woodTimer + PI / 4.0);

    vec4 noisevec = texture3D(woodPerlinNoise3DTexture, MCpositionDistorted);
    if (woodErosion)
        applyErosion(noisevec);
    noisevec *= Noisiness;
    vec3 location = FS_MC_POSITION + noisevec.xyz;

    float distanceWood = length(location.xz) + sqrt(8.0 + location.y) + sqrt(8.0 + abs(location.x));
    distanceWood *= RingFrequency;

    float r = fract(distanceWood + noisevec[0] + noisevec[1] + noisevec[2]) * 2.0;
    if (r > 1.0)
        r = 2.0 - r;

    // vec4 color = mix(LightWoodColor, DarkWoodColor, r);
    vec4 sceneColorLocal = (AnimationGPUComputingMode && ANIMATION_USE_COLOR_PALETTE_SPECTRUM_TRANSITION) ? applyAnimationGPUComputing(FS_SCENE_COLOR) : FS_SCENE_COLOR;
    float alpha = sceneColorLocal.a;
    sceneColorLocal *= intensityLevel;
    float lightIntensity = applyADSLightingModel(woodState, false, FS_NORMAL, FS_POSITION, sceneColorLocal).r;
    vec4 color = mix(mix(LightWoodColor, sceneColorLocal, 0.75), mix(DarkWoodColor, sceneColorLocal, 0.75), r);

    r = fract( (FS_MC_POSITION.x + FS_MC_POSITION.z) * GrainScale + 0.5 );
    noisevec[2] *= r;
    if (r < GrainThreshold)
        color += LightWoodColor * LightGrains * noisevec[2];
    else
        color -= LightWoodColor * DarkGrains * noisevec[2];

    color *=  (lightIntensity + extraLightIntensityFactor);

    vec4 finalColor = min(vec4(color.rgb, 1.0), 1.0);
    if (alpha < 1.0)
        finalColor.a *= (alpha / intensityTransparencyLevel);

    // apply texturing if appropriate
    if (woodTexturing)
        applyTexture(finalColor, gl_TexCoord[0].st);

    // apply per-pixel fog if appriopriate
    gl_FragColor = (woodFog) ? applyFog(finalColor) : finalColor;
}