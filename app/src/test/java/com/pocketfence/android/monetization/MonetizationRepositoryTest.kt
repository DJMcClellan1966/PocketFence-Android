package com.pocketfence.android.monetization

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MonetizationRepository.
 */
class MonetizationRepositoryTest {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: MonetizationRepository
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.apply() } returns Unit
        
        repository = MonetizationRepository(context)
    }
    
    @Test
    fun `getPremiumStatus returns false by default`() {
        every { sharedPreferences.getBoolean(any(), any()) } returns false
        
        val result = repository.getPremiumStatus()
        
        assertFalse(result)
    }
    
    @Test
    fun `setPremiumStatus saves to SharedPreferences`() {
        repository.setPremiumStatus(true)
        
        verify { editor.putBoolean(any(), true) }
        verify { editor.putLong(any(), any()) } // purchase date
        verify { editor.apply() }
    }
    
    @Test
    fun `setPremiumStatus updates StateFlow`() {
        every { sharedPreferences.getBoolean(any(), any()) } returns false
        
        repository.setPremiumStatus(true)
        
        // StateFlow should be updated
        assertEquals(true, repository.isPremium.value)
    }
    
    @Test
    fun `recordAdShown saves timestamp`() {
        repository.recordAdShown()
        
        verify { editor.putLong(any(), any()) }
        verify { editor.apply() }
    }
    
    @Test
    fun `incrementAdClicks increases counter`() {
        every { sharedPreferences.getInt(any(), any()) } returns 5
        
        repository.incrementAdClicks()
        
        verify { editor.putInt(any(), 6) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getAdClicksCount returns stored value`() {
        every { sharedPreferences.getInt(any(), any()) } returns 10
        
        val result = repository.getAdClicksCount()
        
        assertEquals(10, result)
    }
}
