package com.github.rosjoa.todoorganizer.toolWindow

import com.intellij.psi.PsiComment

class PsiCommentWrapper(val psiComment: PsiComment) {

    override fun toString(): String {
        return psiComment.text
    }
}