package ServidorPersistente.OJB;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;
import Dominio.Announcement;
import Dominio.IAnnouncement;
import Dominio.IDisciplinaExecucao;
import Dominio.IExecutionPeriod;
import Dominio.IExecutionYear;
import Dominio.ISite;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.IDisciplinaExecucaoPersistente;
import ServidorPersistente.IPersistentAnnouncement;
import ServidorPersistente.IPersistentExecutionPeriod;
import ServidorPersistente.IPersistentExecutionYear;
import ServidorPersistente.IPersistentSite;

/**
 * @author Ivo Brand�o
 */ 
public class AnnouncementOJBTest extends TestCaseOJB {
	
	private SuportePersistenteOJB persistentSupport = null; 
	private IPersistentAnnouncement persistentAnnouncement = null;
	private IPersistentSite persistentSite = null;
	private IDisciplinaExecucaoPersistente persistentExecutionCourse = null;
	private IPersistentExecutionPeriod persistentExecutionPeriod = null;
	private IPersistentExecutionYear persistentExecutionYear = null;

	private ISite site = null;
	private IDisciplinaExecucao executionCourse = null;
	private IExecutionPeriod executionPeriod = null;
	private IExecutionYear executionYear = null;
	
    public AnnouncementOJBTest(java.lang.String testName) {
        super(testName);
    }
     
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AnnouncementOJBTest.class);
        
        return suite;
    }
    
    protected void setUp() {
        super.setUp();
		try {
			persistentSupport = SuportePersistenteOJB.getInstance();
		} catch (ExcepcaoPersistencia e) {
			e.printStackTrace();
			fail("Error while setting up");
		}
		persistentAnnouncement = persistentSupport.getIPersistentAnnouncement();
		persistentSite = persistentSupport.getIPersistentSite();
		persistentExecutionCourse = persistentSupport.getIDisciplinaExecucaoPersistente();
		persistentExecutionPeriod = persistentSupport.getIPersistentExecutionPeriod();
		persistentExecutionYear = persistentSupport.getIPersistentExecutionYear();
		
		//read existing executionYear
		try {
			persistentSupport.iniciarTransaccao();
			executionYear = persistentExecutionYear.readExecutionYearByName("2002/2003");
			persistentSupport.confirmarTransaccao();
		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
			fail("Error while setting up: readExecutionYearByName");
		}
		assertNotNull(executionYear);

		//read existing executionPeriod
		try {
			persistentSupport.iniciarTransaccao();
			executionPeriod = persistentExecutionPeriod.readByNameAndExecutionYear("2� Semestre", executionYear);
			persistentSupport.confirmarTransaccao();
		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
			fail("Error while setting up: readByNameAndExecutionYear");
		}
		assertNotNull(executionPeriod);

		//read existing executionCourse
		try {
			persistentSupport.iniciarTransaccao();
			executionCourse = persistentExecutionCourse.readByExecutionCourseInitialsAndExecutionPeriod("TFCI", executionPeriod);
			persistentSupport.confirmarTransaccao();
		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
			fail("Error while setting up: readByExecutionCourseInitialsAndExecutionPeriod");
		}
		assertNotNull(executionCourse);

		//read existing site
		try {
			persistentSupport.iniciarTransaccao();
			site = persistentSite.readByExecutionCourse(executionCourse);
			persistentSupport.confirmarTransaccao();
		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
			fail("Error while setting up: readByExecutionCourse");
		}
		assertNotNull(site);
		
    }
    
    protected void tearDown() {
        super.tearDown();
    }
    
    public void testLockWrite() {
		IAnnouncement announcement = null;

        // write non existing
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2003);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DATE, 21);
		Date date = calendar.getTime();

		announcement = new Announcement("newTitle", date, date, "newInformation", this.site);
		System.out.println(announcement);
		System.out.println(this.site);

        try {
        	persistentSupport.iniciarTransaccao();
            persistentAnnouncement.lockWrite(announcement);
            persistentSupport.confirmarTransaccao();
        } catch(ExcepcaoPersistencia excepcaoPersistencia) {
		    fail("testLockWrite: write non Existing");	
        }
        
        IAnnouncement announcementRead = null;        
        try {
            persistentSupport.iniciarTransaccao();
			announcementRead = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("newTitle", date, this.site);
            persistentSupport.confirmarTransaccao();
        } catch(ExcepcaoPersistencia excepcaoPersistencia) {
            fail("testLockWrite: unexpected exception reading");
        }
        assertNotNull(announcementRead);
		assertNotNull(announcementRead);
		assertEquals(announcementRead.getTitle(), "newTitle");
//		assertEquals(announcementRead.getCreationDate(), date);
//		assertEquals(announcementRead.getLastModifiedDate(), date);
		assertEquals(announcementRead.getInformation(), "newInformation");
		assertEquals(announcementRead.getSite(), site);
        
    }

//    public void testDeleteAllAnnouncements() {
//		IAnnouncement announcement = null;
//		
//		//read something
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.YEAR, 2003);
//		calendar.set(Calendar.MONTH, Calendar.JANUARY);
//		calendar.set(Calendar.DATE, 21);
//		Date date = calendar.getTime();
//		
//		try {
//			persistentSupport.iniciarTransaccao();
//			announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("announcement1", date, this.site);
//			persistentSupport.confirmarTransaccao();
//		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
//			fail("testDeleteAllAnnouncements: readAnnouncementByTitleAndDateAndSite");
//		}
//		assertNotNull(announcement);
//
//		//erase all existing        
//        try {
//            persistentSupport.iniciarTransaccao();
//            persistentAnnouncement.deleteAll();
//            persistentSupport.confirmarTransaccao();
//        } catch(ExcepcaoPersistencia ex2) {
//            fail("testDeleteAllAnnouncements: deleteAll");
//        }
//
//		//read something again
//		announcement = null;
//		try {
//			persistentSupport.iniciarTransaccao();
//			announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("announcement1", date, this.site);
//			persistentSupport.confirmarTransaccao();
//		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
//			fail("testDeleteAllAnnouncements: readAnnouncementByTitleAndDateAndSite");
//		}
//		assertNull(announcement);
//    }
//
//    public void testReadAnnouncementByTitleAndDateAndSite() {
//        IAnnouncement announcement = null;
//
//		//read existing
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.YEAR, 2003);
//		calendar.set(Calendar.MONTH, Calendar.JANUARY);
//		calendar.set(Calendar.DATE, 21);
//		Date date = calendar.getTime();
//        try {
//            persistentSupport.iniciarTransaccao();
//            announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("announcement1", date, this.site);
//            persistentSupport.confirmarTransaccao();
//        } catch(ExcepcaoPersistencia excepcaoPersistencia) {
//            fail("testReadAnnouncementByTitleAndDateAndSite: readAnnouncementByTitleAndDateAndSite");
//        }
//        assertNotNull(announcement);
//        assertEquals(announcement.getTitle(), "announcement1");
////		assertEquals(announcement.getCreationDate(), date);
////		assertEquals(announcement.getLastModifiedDate(), date);
//		assertEquals(announcement.getInformation(), "information1");
//		assertEquals(announcement.getSite(), site);
//
//		//read unexisting
//		announcement = null;
//		try {
//			persistentSupport.iniciarTransaccao();
//			announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("unexistingAnnouncement", date, site);
//			persistentSupport.confirmarTransaccao();
//		} catch(ExcepcaoPersistencia excepcaoPersistencia) {
//			fail("testReadAnnouncementByTitleAndDateAndSite: readAnnouncementByTitleAndDateAndSite");
//		}
//		assertNull(announcement);
//	}
//
//    public void testDeleteAnnouncement() {
//        IAnnouncement announcement = null;
//
//		//read existing        
//		Calendar calendar = Calendar.getInstance();
//		calendar.set(Calendar.YEAR, 2003);
//		calendar.set(Calendar.MONTH, Calendar.JANUARY);
//		calendar.set(Calendar.DATE, 21);
//		Date date = calendar.getTime();		
//        try {
//            persistentSupport.iniciarTransaccao();
//			announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("announcement1", date, this.site);
//            persistentSupport.confirmarTransaccao();
//        } catch(ExcepcaoPersistencia ex) {
//            fail("testDeleteAnnouncement: readAnnouncementByTitleAndCreationDateAndSite existing");
//        }
//        assertNotNull(announcement);
//
//		//erase it
//        try {
//            persistentSupport.iniciarTransaccao();
//            persistentAnnouncement.delete(announcement);
//            persistentSupport.confirmarTransaccao();
//        } catch(ExcepcaoPersistencia ex2) {
//            fail("testDeleteAnnouncement: delete");
//        }
//        
//        //read it again
//		announcement = null;
//		try {
//			persistentSupport.iniciarTransaccao();
//			announcement = persistentAnnouncement.readAnnouncementByTitleAndCreationDateAndSite("announcement1", date, this.site);
//			persistentSupport.confirmarTransaccao();
//		} catch(ExcepcaoPersistencia ex) {
//			fail("testDeleteAnnouncement: readAnnouncementByTitleAndCreationDateAndSite unexisting");
//		}
//		assertNull(announcement);
//    }
}