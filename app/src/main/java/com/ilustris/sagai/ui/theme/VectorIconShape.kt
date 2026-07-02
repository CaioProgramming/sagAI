package com.ilustris.sagai.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Shape that follows the filled paths of a vector drawable, scaled to the layout size.
 * Use with [Modifier.dropShadow] / [iconDropShadow] so glow follows the icon silhouette.
 */
class VectorIconShape(
    private val imageVector: ImageVector,
) : Shape {
    private val viewportPath = imageVector.toViewportPath()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        path.addPath(viewportPath)
        if (imageVector.viewportWidth > 0f && imageVector.viewportHeight > 0f) {
            path.transform(
                Matrix().apply {
                    scale(
                        size.width / imageVector.viewportWidth,
                        size.height / imageVector.viewportHeight,
                        1f,
                    )
                },
            )
        }
        return Outline.Generic(path)
    }
}

@Composable
fun Genre?.iconShape() = rememberVectorShape(this?.icon ?: R.drawable.ic_spark)

@Composable
fun rememberVectorShape(
    @DrawableRes drawableRes: Int,
): Shape {
    val imageVector = ImageVector.vectorResource(drawableRes)
    return remember(drawableRes, imageVector) { VectorIconShape(imageVector) }
}

@Composable
fun rememberVectorShape(imageVector: ImageVector): Shape = remember(imageVector) { VectorIconShape(imageVector) }

fun ImageVector.toViewportPath(): Path {
    val target = Path()
    appendGroupPaths(root, Matrix(), target)
    return target
}

private fun appendGroupPaths(
    group: VectorGroup,
    parentMatrix: Matrix,
    target: Path,
) {
    val groupMatrix = Matrix()
    groupMatrix.setFrom(parentMatrix)
    groupMatrix.timesAssign(group.localTransformMatrix())

    group.forEach { node ->
        when (node) {
            is VectorPath -> {
                val path = node.pathData.toPath()
                path.fillType = node.pathFillType
                path.transform(groupMatrix)
                target.addPath(path)
            }

            is VectorGroup -> {
                appendGroupPaths(node, groupMatrix, target)
            }
        }
    }
}

private fun VectorGroup.localTransformMatrix(): Matrix =
    Matrix().apply {
        translate(translationX + pivotX, translationY + pivotY)
        rotateZ(rotation)
        scale(scaleX, scaleY, 1f)
        translate(-pivotX, -pivotY)
    }
