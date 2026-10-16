package com.google.android.cameraview

// Sec-issues#15091: Camera1.getSupportedAspectRatios() remove itens do mapa de proporções de preview
// enquanto itera o keySet(). Com androidx.collection >= 1.4 o iterador lança IllegalArgumentException
// quando há proporção de preview sem tamanho de foto (ex.: tablets Samsung). Este helper fica no pacote
// do cameraview para enxergar CameraView.mImpl, Camera1 e SizeMap (package-private); só o mPreviewSizes
// (private) precisa de reflexão.
object CameraViewAspectRatios {
  private val previewSizesField by lazy {
    Camera1::class.java.getDeclaredField("mPreviewSizes").apply { isAccessible = true }
  }

  // Mesmo resultado do Camera1.getSupportedAspectRatios() (proporções de preview que têm tamanho de foto),
  // mas sem alterar o mapa durante a iteração.
  fun getSupportedAspectRatios(cameraView: CameraView): Set<AspectRatio> {
    val camera1 = cameraView.mImpl as? Camera1 ?: return cameraView.supportedAspectRatios
    val previewSizes = previewSizesField.get(camera1) as SizeMap

    return previewSizes.ratios().filterTo(LinkedHashSet()) { camera1.getAvailablePictureSizes(it) != null }
  }
}
