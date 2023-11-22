package com.the_qa_company.qepmergeprofile;

import com.the_qa_company.qendpoint.compiler.CompiledSail;
import com.the_qa_company.qendpoint.compiler.SparqlRepository;
import com.the_qa_company.qendpoint.core.compact.bitmap.Bitmap64Big;
import com.the_qa_company.qendpoint.core.compact.bitmap.NegBitmap;
import com.the_qa_company.qendpoint.core.exceptions.NotFoundException;
import com.the_qa_company.qendpoint.core.hdt.HDT;
import com.the_qa_company.qendpoint.core.hdt.HDTManager;
import com.the_qa_company.qendpoint.core.hdt.HDTVersion;
import com.the_qa_company.qendpoint.core.options.HDTOptions;
import com.the_qa_company.qendpoint.core.options.HDTOptionsKeys;
import com.the_qa_company.qendpoint.core.triples.IteratorTripleString;
import com.the_qa_company.qendpoint.core.triples.TripleString;
import com.the_qa_company.qendpoint.core.util.StopWatch;
import com.the_qa_company.qendpoint.core.util.io.IOUtil;
import com.the_qa_company.qendpoint.core.util.listener.ColorTool;
import com.the_qa_company.qendpoint.core.util.listener.MultiThreadListenerConsole;
import com.the_qa_company.qendpoint.store.EndpointFiles;
import com.the_qa_company.qendpoint.store.EndpointStore;
import com.the_qa_company.qendpoint.utils.RDFStreamUtils;
import org.apache.commons.io.file.PathUtils;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class QEPMergeProfile {
	public static final byte NFO_START_COMMIT = 0x20;
	public static final byte NFO_END_FILE = 0x21;
	public static void main(String[] args) throws IOException, NotFoundException {
		// test bug
		Rio.createWriter(RDFFormat.NTRIPLES, OutputStream.nullOutputStream());
		MultiThreadListenerConsole console = new MultiThreadListenerConsole(true);
		ColorTool tool = new ColorTool(true);
		tool.setConsole(console);

		Path originFile = Path.of("origin.hdt");
		Path part1 = Path.of("part1.hdt");
		Path part2 = Path.of("part2.hdt");
		Path storeFile = Path.of("qendpoint-profile-repo");
		Path work = Path.of("work");


		// clear old data
		try {
			PathUtils.deleteDirectory(storeFile);
		} catch (NoSuchFileException ignore){}
		try {
			PathUtils.deleteDirectory(work);
		} catch (NoSuchFileException ignore){}

		Files.createDirectories(work);


		tool.log("check test data");
		if (!Files.exists(part1) || !Files.exists(part2)) {
			long n;
			try (HDT hdt = HDTManager.mapHDT(originFile, console)) {
				n = hdt.getTriples().getNumberOfElements();
			}
			tool.log("generating data...");
			// split the dataset in half
			try (Bitmap64Big bm = Bitmap64Big.memory(n)) {
				for (long i = 0; i < n; i += 2) {
					bm.set(i, true); // delete half the dataset
				}

				HDTOptions spec2 = HDTOptions.of(
						HDTOptionsKeys.HDTCAT_LOCATION, work.resolve("hdtcat")
				);
				spec2.set(HDTOptionsKeys.HDTCAT_FUTURE_LOCATION, part1);
				tool.log("part1");
				try (HDT diff = HDTManager.diffBitCatHDTPath(List.of(originFile), List.of(bm), spec2, console)) {
					diff.saveToHDT(part1);
				}
				spec2.set(HDTOptionsKeys.HDTCAT_FUTURE_LOCATION, part2);
				tool.log("part2");
				try (HDT diff = HDTManager.diffBitCatHDTPath(List.of(originFile), List.of(NegBitmap.of(bm)), spec2, console)) {
					diff.saveToHDT(part2);
				}
			}
		}

		int workers = Math.min(Runtime.getRuntime().availableProcessors(), 4);

		HDTOptions spec = HDTOptions.of(
				// profiler
				EndpointStore.QEP_MERGE_PROFILING, "profile.opt",
				// hdtcat location
				HDTOptionsKeys.HDTCAT_LOCATION, work.resolve("qepcat"),
				// use disk indexing
				HDTOptionsKeys.BITMAPTRIPLES_INDEX_METHOD_KEY, HDTOptionsKeys.BITMAPTRIPLES_INDEX_METHOD_VALUE_DISK,
				HDTOptionsKeys.BITMAPTRIPLES_SEQUENCE_DISK_LOCATION, work.resolve("indexing"),
				HDTOptionsKeys.BITMAPTRIPLES_SEQUENCE_DISK, true,

				// workers
				HDTOptionsKeys.BITMAPTRIPLES_DISK_WORKER_KEY, workers,
				HDTOptionsKeys.LOADER_DISK_COMPRESSION_WORKER_KEY, workers,

				// MSD
				HDTOptionsKeys.DICTIONARY_TYPE_KEY, HDTOptionsKeys.DICTIONARY_TYPE_VALUE_MULTI_OBJECTS
		);
		Path part1Foq = part1.resolveSibling(part1.getFileName() + HDTVersion.get_index_suffix("-"));
		if (!Files.exists(part1Foq)) {
			tool.log("missing " + part1Foq + ", building");
			HDTManager.mapIndexedHDT(part1, spec, console).close();
		}
		tool.log("done");

		EndpointFiles files = new EndpointFiles(storeFile);

		Path hdtPath = files.getHDTIndexPath();
		Path hdtPathFoq = files.getHDTIndexV11Path();

		Files.createDirectories(hdtPath.getParent());

		// copy origin file
		Files.copy(part1, hdtPath);
		Files.copy(part1Foq, hdtPathFoq);


		EndpointStore qep = new EndpointStore(files, spec);
		SparqlRepository repo = CompiledSail.compiler().withSourceSail(qep).compileToSparqlRepository();

		repo.init();

		long split = 10_000;

		Path nfo = Path.of("update-nfo.bin");

		try {
			try (
					HDT hdt = HDTManager.mapHDT(part2);
					OutputStream os = new BufferedOutputStream(Files.newOutputStream(nfo))
			) {
				IteratorTripleString it = hdt.searchAll();
				IOUtil.writeString(os, "U-IN");

				tool.getConsole().unregisterAllThreads();
				tool.log("write info");

				long c = 0;

				long start = 0;

				SailRepositoryConnection connection = null;
				try {
					while (it.hasNext()) {
						TripleString next = it.next();
						c++;

						if (connection == null) {
							// open connection
							start = System.currentTimeMillis();
							os.write(NFO_START_COMMIT);
							IOUtil.writeLong(os, start);
							connection = repo.getConnection();
							connection.begin();
						}

						connection.add(RDFStreamUtils.convertStatement(connection.getValueFactory(), next));

						if (c % split == 0) {
							long mid = System.currentTimeMillis();
							IOUtil.writeLong(os, mid);
							tool.log("Start commit after total=" + (mid - start) + "ms");
							connection.commit();
							long end = System.currentTimeMillis();
							tool.log("End commit after total=" + (end - start) + "ms commit=" + (end - mid) + "ms");
							IOUtil.writeLong(os, end);
							connection.close();
							connection = null;
						}
					}

					if (c % split != 0 && connection != null) {
						long mid = System.currentTimeMillis();
						IOUtil.writeLong(os, mid);
						tool.log("Start commit after total=" + (mid - start) + "ms");
						connection.commit();
						long end = System.currentTimeMillis();
						tool.log("End commit after total=" + (end - start) + "ms commit=" + (end - mid) + "ms");
						IOUtil.writeLong(os, end);
						connection.close();
						connection = null;
					}
					os.write(NFO_END_FILE);
				} finally {
					if (connection != null) {
						connection.close();
					}
				}
			}

		} finally {
			repo.shutDown();
		}
	}
}
