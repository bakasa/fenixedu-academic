package pt.utl.ist.codeGenerator.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.sourceforge.fenixedu.domain.Country;
import net.sourceforge.fenixedu.domain.teacher.Category;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import pt.utl.ist.codeGenerator.database.loaders.CategoryLoader;
import pt.utl.ist.codeGenerator.database.loaders.CountryLoader;
import pt.utl.ist.codeGenerator.database.loaders.StudentLoader;
import pt.utl.ist.codeGenerator.database.loaders.TeacherLoader;

public class XlsLoader {

	public static void main(String[] args) {
        final String sourceFilename = args[0];

		try {
			load(sourceFilename);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Load Complete.");
		System.exit(0);
	}

	private static void load(final String sourceFilename) throws IOException, ExcepcaoPersistencia {
        final InputStream inputStream = new FileInputStream(sourceFilename);
		final HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        inputStream.close();

        final Map<String, Country> countries = CountryLoader.load(workbook);
        final Map<String, Category> categories = CategoryLoader.load(workbook);
        StudentLoader.load(workbook, countries);
        TeacherLoader.load(workbook, countries, categories);
	}

}