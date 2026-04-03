#ifndef CoreMLHelper_h
#define CoreMLHelper_h

#import <Foundation/Foundation.h>
#import <CoreML/CoreML.h>
#import <string.h>

/**
 * Helper functions for CoreML interop with Kotlin/Native K2.
 * Inlined in header because cinterop only processes headers, not .m files.
 */

static inline MLMultiArray * _Nullable CoreMLHelper_createFloat32MultiArray(
    NSArray<NSNumber *> * _Nonnull shape,
    const float * _Nonnull floatData,
    NSInteger floatCount
) {
    NSError *error = nil;
    MLMultiArray *array = [[MLMultiArray alloc] initWithShape:shape
                                                     dataType:MLMultiArrayDataTypeFloat32
                                                        error:&error];
    if (!array) return nil;

    float *dest = (float *)array.dataPointer;
    memcpy(dest, floatData, floatCount * sizeof(float));

    return array;
}

static inline void CoreMLHelper_extractFloats(
    MLMultiArray * _Nonnull array,
    float * _Nonnull outBuffer,
    NSInteger bufferSize
) {
    NSInteger count = array.count;
    if (bufferSize < count) count = bufferSize;

    if (array.dataType == MLMultiArrayDataTypeFloat32) {
        memcpy(outBuffer, array.dataPointer, count * sizeof(float));
    } else {
        for (NSInteger i = 0; i < count; i++) {
            outBuffer[i] = array[i].floatValue;
        }
    }
}

#endif /* CoreMLHelper_h */
