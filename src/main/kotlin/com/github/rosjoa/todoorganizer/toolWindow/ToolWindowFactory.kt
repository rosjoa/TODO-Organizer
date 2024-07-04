package com.github.rosjoa.todoorganizer.toolWindow

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory

class ToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val project = toolWindow.project
        private var todoSet : Set<PsiCommentWrapper> = mutableSetOf()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            val psiManager : PsiManager = PsiManager.getInstance(project)
            val psiDirectory = psiManager.findDirectory(project.baseDir)

            if (psiDirectory != null) {
                processDirectory(psiDirectory)
            }

            val jbList = JBList(todoSet)

            jbList.addListSelectionListener {
                val selectedValue = jbList.selectedValue
                if (selectedValue != null) {
                    FileEditorManager.getInstance(project)
                        .openFile(selectedValue.psiComment.containingFile.virtualFile, true)
                    val doc =
                        PsiDocumentManager.getInstance(project).getDocument(selectedValue.psiComment.containingFile)
                    if (doc != null) {
                        WriteCommandAction.runWriteCommandAction(project) {
                            val editor = EditorFactory.getInstance().editors(doc).findFirst().orElse(null)
                            if (editor != null) {
                                val caretModel = editor.caretModel
                                caretModel.moveToOffset(selectedValue.psiComment.startOffset)
                            }
                        }
                    }

                }
            }

            add(jbList)
        }

        private fun processDirectory(psiDirectory : PsiDirectory) {
            for (file in psiDirectory.files) {
                val comments = PsiTreeUtil.collectElementsOfType(file, PsiComment::class.java)
                    .filter { psiComment -> psiComment.text.contains("TODO") }
                    .map { psiComment -> PsiCommentWrapper(psiComment) }
                todoSet = todoSet.plus(comments)
            }

            for (subDir in psiDirectory.subdirectories)
                processDirectory(subDir)
        }
    }
}
