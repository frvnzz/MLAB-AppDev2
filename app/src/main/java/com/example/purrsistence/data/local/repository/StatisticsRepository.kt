package com.example.purrsistence.data.local.repository

import com.example.purrsistence.data.local.dao.Dao
import com.example.purrsistence.data.local.entity.Goal
import com.example.purrsistence.data.local.entity.TrackingSession
import com.example.purrsistence.domain.model.DailyStat
import com.example.purrsistence.domain.model.GoalStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StatisticsRepository(
    private val dao: Dao,
) {
    private val USER_ID = 1 //TODO: for now userID is always 1 for the local user, until accounts are implemented

    fun getWeeklyStats(weekOffset: Int): Flow<Pair<List<DailyStat>, List<GoalStat>>> { //weekly stats with offsets to the weeks from current week as a parameter
        return combine(
            dao.getCompletedSessionsForUser(USER_ID),
            dao.getGoalsRaw(USER_ID)
        ) { sessions, goals ->

            val zone = ZoneId.systemDefault()
            val weekRange = getWeekRange(zone, weekOffset)

            val weekSessions = filterSessionsInRange(sessions, weekRange, zone)

            val dailyStats = mapToDailyStats(weekSessions, zone)
            val goalStats = mapToGoalStats(weekSessions, goals)

            dailyStats to goalStats
        }
    }

    //Week
    private fun getWeekRange( //get the week dates with time zone and an offset for the previous weeks
        zone: ZoneId,
        weekOffset: Int
    ): ClosedRange<LocalDate> {

        val today = LocalDate.now(zone)

        val startOfCurrentWeek = today.with(DayOfWeek.MONDAY)

        val start = startOfCurrentWeek.plusWeeks(weekOffset.toLong())
        val end = start.plusDays(6)

        return start..end
    }

    //Filtering
    private fun filterSessionsInRange( //filter tracked sessions for the correct week date range
        sessions: List<TrackingSession>,
        range: ClosedRange<LocalDate>,
        zone: ZoneId,
    ): List<TrackingSession> {
        return sessions.filter{
            val date = Instant.ofEpochMilli(it.startTime)
                .atZone(zone)
                .toLocalDate()
            date in range
        }
    }


    //Duration
    private fun durationMinutes(session: TrackingSession): Int { //convert duration of tracked session into minutes
        val end = session.endTime ?: System.currentTimeMillis()

        return ((end - session.startTime) /1000 /60).toInt().coerceAtLeast(0)
    }

    //Daily
    private fun mapToDailyStats( //group the tracked sessions per day
        sessions: List<TrackingSession>,
        zone: ZoneId
    ): List<DailyStat> {
        val grouped = sessions.groupBy{
            Instant.ofEpochMilli(it.startTime)
                .atZone(zone)
                .dayOfWeek
        }

        return DayOfWeek.entries.map { day ->
            val total = grouped[day]?.sumOf { durationMinutes(it) } ?: 0
            DailyStat(day, total)
        }
    }


    //Goals
    private fun mapToGoalStats(
        sessions: List<TrackingSession>,
        goals: List<Goal>
    ): List<GoalStat> {
        val goalMap = goals
            .associateBy { it.goalId }

        return sessions
            .groupBy { it.goalId }
            .mapNotNull { (goalId, sessionsForGoal) ->
                val goal = goalMap[goalId] ?: return@mapNotNull null

                val total = sessionsForGoal.sumOf { durationMinutes(it) }

                GoalStat(
                    goalId = goalId,
                    goalName = goal.title,
                    totalMinutes = total
                )
            }.sortedByDescending { it.totalMinutes }
    }

}