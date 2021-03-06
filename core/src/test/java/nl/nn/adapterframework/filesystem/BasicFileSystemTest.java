package nl.nn.adapterframework.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import liquibase.util.StreamUtil;
import nl.nn.adapterframework.configuration.ConfigurationException;

public abstract class BasicFileSystemTest<F, FS extends IBasicFileSystem<F>> extends FileSystemTestBase {

	protected FS fileSystem;
	/**
	 * Returns the file system 
	 * @return fileSystem
	 * @throws ConfigurationException
	 */
	protected abstract FS createFileSystem();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		fileSystem = createFileSystem();
	}
	
	@Override
	public void tearDown() throws Exception {
		fileSystem.close();
		super.tearDown();
	}

	@Test
	public void basicFileSystemTestConfigure() throws Exception {
		fileSystem.configure();
	}

	@Test
	public void basicFileSystemTestOpen() throws Exception {
		fileSystem.configure();
		fileSystem.open();
	}
	
	@Test
	public void basicFileSystemTestExists() throws Exception {
		String filename = "testExists" + FILE1;
		
		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, "tja");
		waitForActionToFinish();
		
		// test
		F f = fileSystem.toFile(filename);
		assertTrue("Expected file[" + filename + "] to be present", fileSystem.exists(f));
	}

	@Test
	public void basicFileSystemTestNotExists() throws Exception {
		String filename = "testNotExists" + FILE1;
		
		fileSystem.configure();
		fileSystem.open();

		deleteFile(null, filename);
		waitForActionToFinish();

		// test
		F f = fileSystem.toFile(filename);
		assertFalse("Expected file[" + filename + "] not to be present", fileSystem.exists(f));
	}


	protected void equalsCheck(String content, String actual) {
		assertEquals(content, actual);
	}


	protected void existsCheck(String filename) throws Exception {
		assertTrue("Expected file [" + filename + "] to be present", _fileExists(filename));
	}



	@Test
	public void basicFileSystemTestDelete() throws Exception {
		String filename = "tobeDeleted" + FILE1;
		
		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, "maakt niet uit");
		waitForActionToFinish();
		existsCheck(filename);

		// test
		F file = fileSystem.toFile(filename);
		fileSystem.deleteFile(file);
		waitForActionToFinish();

		assertFalse("Expected file [" + filename + "] not to be present", _fileExists(filename));
	}

	public void testReadFile(F file, String expectedContents) throws IOException, FileSystemException {
		InputStream in = fileSystem.readFile(file);
		String actual = StreamUtil.getReaderContents(new InputStreamReader(in));
		// test
		equalsCheck(expectedContents.trim(), actual.trim());
	}

	@Test
	public void basicFileSystemTestRead() throws Exception {
		String filename = "read" + FILE1;
		String contents = "Tekst om te lezen";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		// test
		existsCheck(filename);

		F file = fileSystem.toFile(filename);
		// test
		testReadFile(file, contents);
	}

	@Test
	public void basicFileSystemTestGetName() throws Exception {
		String filename = "readName" + FILE1;
		String contents = "Tekst om te lezen";
		
		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();
		
		F file = fileSystem.toFile(filename);
		// test
		assertEquals(filename, fileSystem.getName(file));
	}

	@Test
	public void basicFileSystemTestModificationTime() throws Exception {
		String filename = "readModificationTime" + FILE1;
		String contents = "Tekst om te lezen";
		Date date = new Date();

		fileSystem.configure();
		fileSystem.open();

		createFile(null, filename, contents);
		waitForActionToFinish();

		F file = fileSystem.toFile(filename);
		Date actual = fileSystem.getModificationTime(file);
		long diff = actual.getTime() - date.getTime();

		fileSystem.deleteFile(file);
		waitForActionToFinish();
		// test
		assertFalse(diff > 10000);
	}
	
	
	@Test
	public void basicFileSystemTestMoveFile() throws Exception {
		String fileName = "fileTobeMoved.txt";
		
		fileSystem.configure();
		fileSystem.open();

		createFile(null,fileName, "tja");
		waitForActionToFinish();
		
		assertTrue(_fileExists(fileName));
		
		String destinationFolder = "destinationFolder";
		_createFolder(destinationFolder);
		waitForActionToFinish();

		assertTrue(_fileExists(fileName));
		assertTrue(_folderExists(destinationFolder));

		F f = fileSystem.toFile(fileName);
		F movedFile =fileSystem.moveFile(f, destinationFolder, false);
		waitForActionToFinish();
		
		
		assertTrue("Destination folder must exist",_folderExists(destinationFolder));
		assertTrue("Destination must exist ["+fileSystem.getName(movedFile)+"]",fileSystem.exists(movedFile));
		//TODO: test that contents of file has remained the same
		//TODO: test that file timestamp has not changed
		assertFalse("Origin must have disappeared",_fileExists(fileName));
	}



	
	@Test
	public void basicFileSystemTestExistsMethod() throws Exception {
		String fileName = "fileExists.txt";

		fileSystem.configure();
		fileSystem.open();

		createFile(null, fileName, "");
		waitForActionToFinish();
		F f = fileSystem.toFile(fileName);

		assertTrue(fileSystem.exists(f));
	}

	public void basicFileSystemTestListFile(String folder) throws Exception {
		String contents1 = "maakt niet uit";
		String contents2 = "maakt ook niet uit";
		
		fileSystem.configure();
		fileSystem.open();

		long beforeFilesCreated=System.currentTimeMillis();
		
		createFile(folder, FILE1, contents1);
		createFile(folder, FILE2, contents2);
		waitForActionToFinish();

		long afterFilesCreated=System.currentTimeMillis();
		
		Set<F> files = new HashSet<F>();
		Set<String> filenames = new HashSet<String>();
		Iterator<F> it = fileSystem.listFiles(folder);
		int count = 0;
		// Count files
		while (it.hasNext()) {
			F f=it.next();
			files.add(f);
			String name=fileSystem.getName(f);
			log.debug("found file ["+name+"]");
			filenames.add(name);
			count++;
		}

		assertEquals("Size of set of files", 2, files.size());
		assertEquals("Size of set of filenames", 2, filenames.size());
		
		if (folder==null) {
			for (String filename:filenames) {
				F f=fileSystem.toFile(filename);
				assertNotNull("file must be found by filename ["+filename+"]",f);
				assertTrue("file must exist when referred to by filename ["+filename+"]",fileSystem.exists(f));
			}
		}
		
		it = fileSystem.listFiles(folder);
		for (int i = 0; i < count; i++) {
			assertTrue(it.hasNext());
			it.next();
		}
		// test
		assertFalse(it.hasNext());

		deleteFile(folder, FILE1);
		int numDeleted = 1;
		
		waitForActionToFinish();
		
		it = fileSystem.listFiles(folder);
		for (int i = 0; i < count - numDeleted; i++) {
			assertTrue(it.hasNext());
			F f=it.next();
			log.debug("found file ["+fileSystem.getName(f)+"]");
			long modTime=fileSystem.getModificationTime(f).getTime();
			if (doTimingTests) assertTrue("modtime ["+modTime+"] not after t0 ["+beforeFilesCreated+"]", modTime>=beforeFilesCreated);
			if (doTimingTests) assertTrue("modtime ["+modTime+"] not before t1 ["+afterFilesCreated+"]", modTime<=afterFilesCreated);
		}
		// test
		assertFalse("after a delete the number of files should be one less",it.hasNext());
		
		Thread.sleep(1000);
		it = fileSystem.listFiles(folder);
		for (int i = 0; i < count - numDeleted; i++) {
			assertTrue(it.hasNext());
			F f=it.next();
			long modTime=fileSystem.getModificationTime(f).getTime();
			if (doTimingTests) assertTrue("modtime ["+modTime+"] not after t0 ["+beforeFilesCreated+"]", modTime>=beforeFilesCreated);
			if (doTimingTests) assertTrue("modtime ["+modTime+"] not before t1 ["+afterFilesCreated+"]", modTime<=afterFilesCreated);
		}

		deleteFile(folder, FILE2);
		numDeleted++;

		it = fileSystem.listFiles(folder);
		for (int i = 0; i < count - numDeleted; i++) {
			assertTrue(it.hasNext());
			it.next();
		}
		// test
		assertFalse(it.hasNext());
	}
	
	@Test
	public void basicFileSystemTestListFileFromRoot() throws Exception {
		basicFileSystemTestListFile(null);
	}
	@Test
	public void basicFileSystemTestListFileFromFolder() throws Exception {
		_createFolder("folder");
		basicFileSystemTestListFile("folder");
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromOtherFoldersWhenReadingFromRoot() throws Exception {
		_createFolder("folder");
		_createFolder("Otherfolder");
		createFile("Otherfolder", "otherfile", "maakt niet uit");
		basicFileSystemTestListFile(null);
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromOtherFoldersWhenReadingFromFolder() throws Exception {
		_createFolder("folder");
		_createFolder("Otherfolder");
		createFile("Otherfolder", "otherfile", "maakt niet uit");
		basicFileSystemTestListFile("folder");
	}

	@Test
	public void basicFileSystemTestListFileShouldNotReadFromRootWhenReadingFromFolder() throws Exception {
		_createFolder("folder");
		createFile(null, "otherfile", "maakt niet uit");
		basicFileSystemTestListFile("folder");
	}
	@Test
	public void basicFileSystemTestListFileShouldNotReadFolders() throws Exception {
		String contents1 = "maakt niet uit";
		String contents2 = "maakt ook niet uit";
		String folderName = "subfolder";
		
		fileSystem.configure();
		fileSystem.open();

		
		createFile(null, FILE1, contents1);
		createFile(null, FILE2, contents2);
		_createFolder(folderName);

		Set<F> files = new HashSet<F>();
		Set<String> filenames = new HashSet<String>();
		Iterator<F> it = fileSystem.listFiles(null);
		int count = 0;
		// Count files
		while (it.hasNext()) {
			F f=it.next();
			files.add(f);
			filenames.add(fileSystem.getName(f));
			count++;
		}

		assertEquals("Size of set of files, should not contain folders", 2, files.size());
		assertEquals("Size of set of filenames, should not contain folders", 2, filenames.size());
		
	}

}