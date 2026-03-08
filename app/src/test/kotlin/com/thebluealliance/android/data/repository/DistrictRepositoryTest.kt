package com.thebluealliance.android.data.repository

import com.thebluealliance.android.data.local.TBADatabase
import com.thebluealliance.android.data.local.dao.DistrictDao
import com.thebluealliance.android.data.local.dao.DistrictRankingDao
import com.thebluealliance.android.data.remote.TbaApi
import com.thebluealliance.android.data.remote.dto.DistrictDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DistrictRepositoryTest {

    private val api: TbaApi = mockk()
    private val db: TBADatabase = mockk(relaxed = true) {
        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        every { queryExecutor } returns executor
        every { transactionExecutor } returns executor
    }
    private val districtDao: DistrictDao = mockk(relaxUnitFun = true)
    private val districtRankingDao: DistrictRankingDao = mockk(relaxUnitFun = true)

    private val repo = DistrictRepository(api, db, districtDao, districtRankingDao)

    @Test
    fun `getDistrictHistory returns years sorted descending`() = runTest {
        val dtos = listOf(
            DistrictDto(abbreviation = "ne", displayName = "New England", key = "2022ne", year = 2022),
            DistrictDto(abbreviation = "ne", displayName = "New England", key = "2024ne", year = 2024),
            DistrictDto(abbreviation = "ne", displayName = "New England", key = "2023ne", year = 2023),
        )
        coEvery { api.getDistrictHistory("ne") } returns dtos

        val districts = repo.getDistrictHistory("ne")

        assertEquals(2024, districts[0].year)
        assertEquals(2023, districts[1].year)
        assertEquals(2022, districts[2].year)
    }

    @Test
    fun `getDistrictHistory combines equivalent abbreviations`() = runTest {
        // CHS changed to FCH in 2026
        val chsDtos = listOf(
            DistrictDto(abbreviation = "chs", displayName = "FIRST Chesapeake", key = "2024chs", year = 2024),
            DistrictDto(abbreviation = "chs", displayName = "FIRST Chesapeake", key = "2025chs", year = 2025),
        )
        val fchDtos = listOf(
            DistrictDto(abbreviation = "fch", displayName = "FIRST Chesapeake", key = "2026fch", year = 2026),
        )
        coEvery { api.getDistrictHistory("chs") } returns chsDtos
        coEvery { api.getDistrictHistory("fch") } returns fchDtos

        val districts = repo.getDistrictHistory("fch")

        assertEquals(listOf(2026, 2025, 2024), districts.map { it.year })
        assertEquals(listOf("2026fch", "2025chs", "2024chs"), districts.map { it.key })
    }

    @Test
    fun `getDistrictHistory returns empty list when API returns empty`() = runTest {
        coEvery { api.getDistrictHistory("ne") } returns emptyList()

        val districts = repo.getDistrictHistory("ne")

        assertEquals(0, districts.size)
    }
}
