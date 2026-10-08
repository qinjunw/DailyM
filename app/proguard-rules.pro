# sherpa-onnx JNI reads configuration fields and class descriptors by name.
# Preserve the native interface, including fields not referenced from Kotlin.
-keep class com.k2fsa.sherpa.onnx.** { *; }
