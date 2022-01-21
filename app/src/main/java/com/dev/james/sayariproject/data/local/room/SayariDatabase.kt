package com.dev.james.sayariproject.data.local.room

import androidx.room.CoroutinesRoom
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dev.james.sayariproject.di.modules.ApplicationScope
import com.dev.james.sayariproject.models.discover.ActiveMissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [ActiveMissions::class] , version = 1)
abstract class SayariDatabase : RoomDatabase() {

    abstract fun dbDao() : Dao

    class Callback @Inject constructor(
        private val database : Provider<SayariDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().dbDao()

            applicationScope.launch {
                dao.addMissions(
                    ActiveMissions(1 , "LRO" , "https://upload.wikimedia.org/wikipedia/commons/c/c4/LRO_mission_logo_%28transparent_background%29_01.png","Moon" )
                )
                dao.addMissions(
                    ActiveMissions(2 , "GRAIL" , "https://4.bp.blogspot.com/-Ez9fd76ycmc/UM-wpD3TKlI/AAAAAAAANAE/NiZRw12DP6s/s1600/GRAIL-logo.JPG","Moon" )
                )
                dao.addMissions(
                    ActiveMissions(3 , "Chang'e" , "https://skyandtelescope.org/wp-content/uploads/Change_5_Mission_Patch-600x600.png","Moon" )
                )
                dao.addMissions(
                    ActiveMissions(4 , "Chandryan" , "https://2.bp.blogspot.com/-cFW67V-SUBk/WMHlGYToswI/AAAAAAAA6X4/7CaK_fEWSjQC_8Ye8QVtpXKZRsrZUiKBgCLcB/s1600/Chandrayaan-logo.jpg","Moon" )
                )
                dao.addMissions(
                    ActiveMissions(5 , "Curiosity" , "https://mars.nasa.gov/system/resources/detail_files/24863_MSLCuriosityStickerTemplate-web.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(6 , "Persaverance" , "https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/Mars_2020_mission_patch.png/676px-Mars_2020_mission_patch.png","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(7 , "Inginuity" , "https://mars.nasa.gov/system/resources/list_images/24998_sticker-thm.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(8 , "Hope" , "http://www.collectspace.com/review/uae_hope_emirates_mars_mission_patch02-lg.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(9 , "Tianwen1" , "https://pbs.twimg.com/media/EvCyB4xVIAcfLKT.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(10 , "Insight" , "https://upload.wikimedia.org/wikipedia/commons/3/3c/InSight_mission_patch_v1.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(11 , "Exomars" , "https://2.bp.blogspot.com/-72mFxMFuzv4/W-YgAGMy57I/AAAAAAABJLY/WjoFBw8XrdgDj61F6RJyhwHUbgnGJrxgwCLcBGAs/s1600/logo-exomars.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(12 , "Maven" , "https://upload.wikimedia.org/wikipedia/commons/7/70/MAVEN_Mission_Logo.png","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(13 , "MRO" , "https://upload.wikimedia.org/wikipedia/commons/2/26/Mars_Reconnaissance_Orbiter_-_MOI_Flight_Ops_Team_Patch.png","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(14 , "Mars Express" , " https://4.bp.blogspot.com/-VvVcQmD9zR0/UBAgaedn37I/AAAAAAAAHAI/pGC7ORdbGqo/s1600/marsexpress_logo_high.jpg","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(15 , "Mars Odyssey" , "https://en-academic.com/pictures/enwiki/50/2001_Mars_Odyssey_-_mars-odyssey-logo-sm.png","Mars" )
                )
                dao.addMissions(
                    ActiveMissions(16 , "Bepi Colombo" , "https://www.esa.int/var/esa/storage/images/esa_multimedia/images/2015/04/bepicolombo3/15367984-1-eng-GB/BepiColombo_card_medium.jpg","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(17 , "Lucy" , "https://blogs.nasa.gov/lucy/wp-content/uploads/sites/323/2021/10/Lucy-logo.jpg","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(18 , "Osiris rex" , "https://www.asteroidmission.org/wp-content/uploads/2015/09/OSIRIS-REx-Mission-Logo1.png","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(19 , "JUNO" , "https://upload.wikimedia.org/wikipedia/commons/3/32/Juno_mission_insignia.svg","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(20 , "Cassini" , "https://i.etsystatic.com/10397404/r/il/473542/1554355909/il_fullxfull.1554355909_mevl.jpg","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(21 , "New Horizons" , "https://upload.wikimedia.org/wikipedia/commons/f/fe/New_Horizons_Pluto-Kuiper_Belt_Mission_patch.png","Solar System" )
                )
                dao.addMissions(
                    ActiveMissions(22 , "Hubble" , "https://www.nasa.gov/sites/default/files/thumbnails/image/sts125-patch-hi.jpg","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(23 , "JWST" , "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/JWST_decal.svg/1200px-JWST_decal.svg.png","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(24 , "Chandra" , "https://chandra.harvard.edu/graphics/resources/illustrations/misc/cxo-patch-72.jpg","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(25 , "IXPE" , "https://blogs.nasa.gov/ixpe/wp-content/uploads/sites/329/2021/10/IXPE-logo--300x209.png","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(26 , "Spitzer" , "https://astrobiology.nasa.gov/uploads/filer_public_thumbnails/filer_public/1a/5e/1a5e20c7-3026-465e-88d2-f04cfb49d7bc/spitzer-patch-500png230x0_q85.png__270x197_q85_crop_subsampling-2.png","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(27 , "Wise" , "http://4.bp.blogspot.com/-axgZkMJmX2I/TjDwCFJZIqI/AAAAAAAAB38/UTR7gztukMg/s1600/NASA+-+WISE+patch.jpg","Astronomy" )
                )
                dao.addMissions(
                    ActiveMissions(28 , "Kepler" , "https://i.pinimg.com/originals/61/5d/e0/615de0ee88db7d21f6c7149d254bf80f.jpg","Exoplanets" )
                )
                dao.addMissions(
                    ActiveMissions(29 , "Tess" , "http://www.collectspace.com/review/mit_tess_sciopspatch01-lg.jpg","Exoplanets" )
                )
                dao.addMissions(
                    ActiveMissions(30 , "Parker Solar Probe" , "https://blogs.nasa.gov/parkersolarprobe/wp-content/uploads/sites/274/2018/08/Parker-Solar-Probe_Logo-1024x1024.png","Sun" )
                )
                dao.addMissions(
                    ActiveMissions(31 , "SOHO" , "https://upload.wikimedia.org/wikipedia/en/a/a7/SOHO_insignia.png","Sun" )
                )

            }
        }

    }

}