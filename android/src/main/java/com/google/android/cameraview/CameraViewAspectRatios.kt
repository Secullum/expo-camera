package com.google.android.cameraview

// Camera1.getSupportedAspectRatios() remove itens do mapa de proporções de preview
// enquanto itera o keySet(). A partir do androidx.collection 1.3 (conferido até a 1.4.2)
// o iterador lança IllegalArgumentException quando alguma proporção de preview sem tamanho
// de foto não é a última da iteração (ex.: tablets Samsung).
// Este helper fica no pacote do cameraview para enxergar CameraView.mImpl,
// Camera1 e SizeMap (package-private); só o mPreviewSizes (private) precisa de reflexão.
object CameraViewAspectRatios {
  private val previewSizesField by lazy {
    Camera1::class.java.getDeclaredField("mPreviewSizes").apply { isAccessible = true }
  }

  // Devolve as proporções de preview que têm tamanho de foto
  // (o mesmo filtro que o Camera1.getSupportedAspectRatios() aplica),
  // sem alterar o mapa: o mPreviewSizes não perde as proporções sem foto.
  fun getSupportedAspectRatios(cameraView: CameraView): Set<AspectRatio> {
    val camera1 = cameraView.mImpl as? Camera1 ?: return cameraView.supportedAspectRatios
    val previewSizes = previewSizesField.get(camera1) as SizeMap

    return previewSizes.ratios().filterTo(LinkedHashSet()) { camera1.getAvailablePictureSizes(it) != null }
  }
}
