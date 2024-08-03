package com.khm.group.center.datatype.summary

class ProjectSummary(
    val projectName: String,
) {
    val taskList = mutableListOf<TaskSummary>()

    val projectUseTimes: Int
        get() = taskList.size

    fun checkIsSameProject(taskSummary: TaskSummary): Boolean {
        return taskSummary.projectName == projectName
    }

    fun updateByTask(taskSummary: TaskSummary):Boolean {
        if (!checkIsSameProject(taskSummary)) return false

        if (taskList.contains(taskSummary)) return false

        taskList.add(taskSummary)

        return true
    }

    override fun hashCode(): Int {
        return projectName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        // Check other is ProjectSummary
        if (other is ProjectSummary)
            return projectName == (other as ProjectSummary).projectName

        // Check other is TaskSummary
        if (other is TaskSummary)
            return projectName == (other as TaskSummary).projectName

        return false
    }
}