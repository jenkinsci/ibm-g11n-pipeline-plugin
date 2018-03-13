/*
 * Copyright IBM Corp ©. 2017,2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jenkinsci.plugins.gpjenkins;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Maven;
import hudson.tasks.Shell;
import hudson.tasks.BuildStepDescriptor;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.g11n.pipeline.client.BundleData;
import com.ibm.g11n.pipeline.client.NewBundleData;
import com.ibm.g11n.pipeline.client.ServiceAccount;
import com.ibm.g11n.pipeline.client.ServiceClient;
import com.ibm.g11n.pipeline.client.ServiceException;
import com.ibm.g11n.pipeline.client.rb.CloudResourceBundleControl;
import com.ibm.g11n.pipeline.resfilter.FilterOptions;
import com.ibm.g11n.pipeline.resfilter.LanguageBundle;
import com.ibm.g11n.pipeline.resfilter.LanguageBundleBuilder;
import com.ibm.g11n.pipeline.resfilter.ResourceFilter;
import com.ibm.g11n.pipeline.resfilter.ResourceFilterException;
import com.ibm.g11n.pipeline.resfilter.ResourceFilterFactory;
import com.ibm.g11n.pipeline.resfilter.ResourceString;
import com.ibm.g11n.pipeline.client.NewResourceEntryData;
import com.ibm.g11n.pipeline.client.ResourceEntryData;

import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link GlobalizationPipelineBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #instanceId})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Parth Gaglani
 */
public class GlobalizationPipelineBuilder extends Builder implements SimpleBuildStep {


	private String instanceId;
	private String url;
	private String userId;
	private String password;
	private String goalType;
	private String baseDir;
	private String includeRule;
	private String excludeRule;
	private String srcLang;
	private String type;
	private String langMap;
	private String languageIdStyle;
	private Boolean outputSourceLang;
	private String outputContentOption;
	private String bundleLayout;
	private String outDir;
	private Boolean overwrite;




	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public GlobalizationPipelineBuilder(String instanceId, String url, String userId,
			String password, String goalType,String baseDir,String includeRule,
			String excludeRule, String srcLang, String type, String langMap,
			String languageIdStyle, Boolean outputSourceLang, String outputContentOption,
			String bundleLayout, String outDir, Boolean overwrite
			) {

		this.instanceId = instanceId;
		this.url = url;
		this.userId = userId;
		this.password = password;
		this.goalType = goalType;
		this.baseDir = baseDir;
		this.includeRule = includeRule;
		this.excludeRule = excludeRule;
		this.srcLang = srcLang;
		this.type = type;
		this.langMap = langMap;
		this.languageIdStyle = languageIdStyle;
		this.outputSourceLang = outputSourceLang;
		this.outputContentOption = outputContentOption;
		this.bundleLayout = bundleLayout;
		this.outDir = outDir;
		this.overwrite = overwrite;

	}


	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getLangMap() {
		return langMap;
	}

	public void setLangMap(String langMap) {
		this.langMap = langMap;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGoalType() {
		return goalType;
	}

	public void setGoalType(String goalType) {
		this.goalType = goalType;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getSrcLang() {
		return srcLang;
	}

	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIncludeRule() {
		return includeRule;
	}

	public void setIncludeRule(String includeRule) {
		this.includeRule = includeRule;
	}

	public String getExcludeRule() {
		return excludeRule;
	}

	public void setExcludeRule(String excludeRule) {
		this.excludeRule = excludeRule;
	}

	public String getLanguageIdStyle() {
		return languageIdStyle;
	}

	public void setLanguageIdStyle(String languageIdStyle) {
		this.languageIdStyle = languageIdStyle;
	}

	public Boolean getOutputSourceLang() {
		return outputSourceLang;
	}

	public void setOutputSourceLang(Boolean outputSourceLang) {
		this.outputSourceLang = outputSourceLang;
	}

	public String getOutputContentOption() {
		return outputContentOption;
	}

	public void setOutputContentOption(String outputContentOption) {
		this.outputContentOption = outputContentOption;
	}

	public String getBundleLayout() {
		return bundleLayout;
	}

	public void setBundleLayout(String bundleLayout) {
		this.bundleLayout = bundleLayout;
	}

	public String getOutDir() {
		return outDir;
	}

	public void setOutDir(String outDir) {
		this.outDir = outDir;
	}

	public Boolean getOverwrite() {
		return overwrite;
	}

	public void setOverwrite(Boolean overwrite) {
		this.overwrite = overwrite;
	}

	public String computeParentFromBaseDir(FilePath filepath) throws IOException, InterruptedException{
		String parent = "";
		if(filepath.getParent() != null){
			parent = filepath.getParent().toURI().getPath();
			parent = parent.substring(parent.lastIndexOf(getBaseDir()), parent.length());
			parent = parent.replaceAll(getBaseDir() + "/", "");
		}
		return parent;
	}

	public String printFilesConsidered(FilePath[] filePaths) throws IOException, InterruptedException{
		String filesConsidered = "";
		StringBuffer filesConsideredBuffer = new StringBuffer(filesConsidered);
		for(FilePath filePath : filePaths){
			filesConsideredBuffer.append(computeParentFromBaseDir(filePath) + filePath.getName() + '\n');
		}
		filesConsidered = filesConsideredBuffer.toString();
		return filesConsidered;
	}

	private String pathToBundleId(String type, FilePath path) throws IOException, InterruptedException {

		FilePath parent = path.getParent();
		String pkgName = "";
		if(type.equals("java")){
			pkgName = parent == null ? "" :
				computeParentFromBaseDir(path).replace(File.separatorChar, '.');
		}
		else{
			pkgName = parent == null ? "" :
				computeParentFromBaseDir(path).replace(File.separatorChar, '-');
		}
		String fileName = path.getName().replaceAll(" ", "_");
		if (type.equals("java")) {
			int dotIdx = fileName.indexOf('.');
			if (dotIdx >= 0) {
				fileName = fileName.substring(0, dotIdx);
			}
			if(pkgName.equals("")){
				return fileName;
			}
			return pkgName + fileName;
		}

		return pkgName + fileName;
	}

	private String getResourceType(String type){
		return type.toUpperCase();
	}


	private String getLanguageId(String gpLanguageTag, String langIdStyle,
			Map<String, String> langMappingMap) {
		String languageId = gpLanguageTag;
		if (langMappingMap != null) {
			String mappedId = langMappingMap.get(gpLanguageTag);
			if (mappedId != null) {
				languageId = mappedId;
			}
		}
		switch (langIdStyle) {
		case "bcp47_underscore":
			languageId = languageId.replace('-', '_');
			break;
		case "bcp47":
			// do nothing
			break;
		default:
			languageId = languageId.replace('-', '_');
			break;
		}
		return languageId;
	}

	private String getEmbeddedLanguageId(String gpLanguageTag, Map<String, String> langMap) {
		String languageId = gpLanguageTag;
		if (langMap != null) {
			String mappedId = langMap.get(gpLanguageTag);
			if (mappedId != null) {
				languageId = mappedId;
			}
		}
		return languageId;
	}


	private void mergeTranslation(LanguageBundle bundle, String language, String type,
			FilePath srcFile, FilePath outFile) throws InterruptedException, IOException {
		ResourceFilter filter = ResourceFilterFactory.getResourceFilter(type);
		try (OutputStream fos = outFile.write();
				InputStream fis = srcFile.read()) {
			filter.merge(fis, fos, bundle, new FilterOptions(Locale.forLanguageTag(language)));
		} catch (IOException e) {
			throw new IOException("I/O error while merging the translated strings to " + outFile.getName(), e);
		} catch (ResourceFilterException e) {
			throw new IOException("Resource filter error while merging the translated strings to " + outFile.getName(), e);
		}
	}

	private void exportTranslation(LanguageBundle bundle, String language, String type,
			FilePath outFile) throws IOException, InterruptedException {
		ResourceFilter filter = ResourceFilterFactory.getResourceFilter(type);
		try (OutputStream fos = outFile.write()) {
			filter.write(fos, bundle, new FilterOptions(Locale.forLanguageTag(language)));
		} catch (IOException e) {
            throw new IOException("I/O error while writing the translated strings to "
                    + outFile.getName(), e);
        } catch (ResourceFilterException e) {
            throw new IOException("Resource filter error while writing the translated strings to "
                    + outFile.getName(), e);
        }
	}

	private LanguageBundle getBundle(ServiceClient client, String bundleId, String language, String embeddedLanguageId,
			boolean reviewedOnly, boolean withFallback) throws ServiceException {
		try {
			LanguageBundleBuilder bundleBuilder = new LanguageBundleBuilder(false);
			bundleBuilder.embeddedLanguageCode(embeddedLanguageId);
			Map<String, ResourceEntryData> resEntries = client.getResourceEntries(bundleId, language);
			for (Entry<String, ResourceEntryData> entry : resEntries.entrySet()) {
				String key = entry.getKey();
				ResourceEntryData data = entry.getValue();
				String resVal = data.getValue();
				String srcVal = data.getSourceValue();
				Integer seqNum = data.getSequenceNumber();
				List<String> notes = data.getNotes();

				if (reviewedOnly) {
					if (!data.isReviewed()) {
						resVal = null;
					}
				}

				if (resVal == null && withFallback) {
					resVal = data.getSourceValue();
				}

				if (resVal != null) {
					ResourceString.Builder resb = ResourceString.with(key, resVal).sourceValue(srcVal);
					if (seqNum != null) {
						resb.sequenceNumber(seqNum.intValue());
					}
					if (notes != null) {
						resb.notes(notes);
					}
					bundleBuilder.addResourceString(resb);
				}
			}
			return bundleBuilder.build();
		} catch (ServiceException e) {
			throw new ServiceException("Globalization Pipeline service error", e);
		}
	}

	private void exportLanguageResource(ServiceClient client, FilePath bf, String language,
			FilePath outBaseDir, String outContntOpt, String bundleLayout,
			String langIdStyle, String srcLang, Map<String, String> langMap, TaskListener listener) throws ServiceException, IOException, InterruptedException
	{
		String srcFileName = bf.getName();
		String relPath = computeParentFromBaseDir(bf);
		FilePath outputFile = null;

		switch (bundleLayout) {
		case "lang_suffix": {
			FilePath dir = new FilePath(outBaseDir, relPath);
			String tgtName = srcFileName;
			// Compose file name if the output language is not the source language
			if (!language.equals(srcLang)) {
				String baseName = srcFileName;
				String extension = "";
				int extensionIndex = srcFileName.lastIndexOf('.');
				if (extensionIndex > 0) {
					baseName = srcFileName.substring(0, extensionIndex);
					extension = srcFileName.substring(extensionIndex);
				}

				// checks if the source file's base name (without extension) ends with
				// source language code suffix, e.g. foo_en => foo
				String srcLangSuffix = "_" + getLanguageId(srcLang, langIdStyle, langMap);
				if (baseName.endsWith(srcLangSuffix)) {
					// truncates source the source language suffix from base name
					baseName = baseName.substring(0, baseName.length() - srcLangSuffix.length());
				}

				// append target language suffix to the base name, e.g. foo => foo_de
				tgtName = baseName + "_" + getLanguageId(language, langIdStyle, langMap) + extension;
			}
			outputFile = new FilePath(dir, tgtName);
			break;
		}
		case "lang_only": {
			FilePath dir = (new FilePath(outBaseDir, relPath)).getParent();
			int extensionIndex = srcFileName.lastIndexOf('.');
			String extension = extensionIndex >= 0 ?
					srcFileName.substring(extensionIndex) : "";
					String baseName = getLanguageId(language, langIdStyle, langMap);
					outputFile = new FilePath(dir, baseName + extension);
					break;
		}
		case "lang_subdir": {
			FilePath dir = new FilePath(outBaseDir, relPath);
			FilePath langSubDir = new FilePath(dir, getLanguageId(language, langIdStyle, langMap));
			outputFile = new FilePath(langSubDir, srcFileName);
			break;
		}
		case "lang_dir":{
			FilePath dir = (new FilePath(outBaseDir, relPath)).getParent();
			FilePath langDir = new FilePath(dir, getLanguageId(language, langIdStyle, langMap));
			outputFile = new FilePath(langDir, srcFileName);
			break;
		}
		default: 
			FilePath dir = new FilePath(outBaseDir, relPath);
			String tgtName = srcFileName;
			// Compose file name if the output language is not the source language
			if (!language.equals(srcLang)) {
				String baseName = srcFileName;
				String extension = "";
				int extensionIndex = srcFileName.lastIndexOf('.');
				if (extensionIndex > 0) {
					baseName = srcFileName.substring(0, extensionIndex);
					extension = srcFileName.substring(extensionIndex);
				}

				// checks if the source file's base name (without extension) ends with
				// source language code suffix, e.g. foo_en => foo
				String srcLangSuffix = "_" + getLanguageId(srcLang, langIdStyle, langMap);
				if (baseName.endsWith(srcLangSuffix)) {
					// truncates source the source language suffix from base name
					baseName = baseName.substring(0, baseName.length() - srcLangSuffix.length());
				}

				// append target language suffix to the base name, e.g. foo => foo_de
				tgtName = baseName + "_" + getLanguageId(language, langIdStyle, langMap) + extension;
			}
			outputFile = new FilePath(dir, tgtName);
			break;
		}

		//		if (outputFile == null) {
		//			throw new InterruptedException("Failed to resolve output directory");
		//		}

		listener.getLogger().println("Exporting bundle:" + pathToBundleId(getType(), bf) + " language:" + language + " to "
				+ outputFile.toURI().getPath());

		if (outputFile.exists()) {
			if (overwrite) {
				listener.getLogger().println("The output bundle file:" + outputFile.toURI().getPath()
						+ " already exists - overwriting");
			} else {
				listener.getLogger().println("The output bundle file:" + outputFile.toURI().getPath()
						+ " already exists - skipping");
				// When overwrite is false, do nothing
				return;
			}
		}

		if (!outputFile.getParent().exists()) {
			outputFile.getParent().mkdirs();
		}

		LanguageBundle bundle;
		String embeddedLangId = getEmbeddedLanguageId(language, langMap);

		switch (outContntOpt) {
		case "merge_to_src":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, false, true);
			mergeTranslation(bundle, language, getResourceType(getType()), bf, outputFile);
			break;

		case "trans_with_fallback":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, false, true);
			exportTranslation(bundle, language, getResourceType(getType()), outputFile);
			break;

		case "trans_only":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, false, false);
			exportTranslation(bundle, language, getResourceType(getType()), outputFile);
			break;

		case "merge_reviewed_to_src":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, true, true);
			mergeTranslation(bundle, language, getResourceType(getType()), bf, outputFile);
			break;

		case "reviewed_with_fallback":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, true, true);
			exportTranslation(bundle, language, getResourceType(getType()), outputFile);
			break;

		case "reviewed_only":
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, true, false);
			exportTranslation(bundle, language, getResourceType(getType()), outputFile);
			break;

		default:
			bundle = getBundle(client, pathToBundleId(getType(), bf), language, embeddedLangId, false, true);
			mergeTranslation(bundle, language, getResourceType(getType()), bf, outputFile);
			break;
		}
	}


	@Override
	public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
		// This is where you 'build' the project.

		// This also shows how you can consult the global configuration of the builder


		listener.getLogger().println("*************** IBM GLOBALIZATION PIPELINE BUILDSTEP starting... ***************");

		// CHECKING NULLS
		if(url == null || instanceId == null || userId == null || password == null){
			listener.getLogger().println("Null credentials.. Something went wrong.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(baseDir == null || workspace == null || includeRule == null || excludeRule == null){
			listener.getLogger().println("Null paths on workspace or rules you specified.. Something went wrong.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(srcLang == null){
			listener.getLogger().println("Null Source Language. Please check again");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(langMap == null){
			listener.getLogger().println("Null Target Languages and mapping. Please check again");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(outDir == null){
			listener.getLogger().println("Null Output Directory. Please check again");
			build.setResult(Result.UNSTABLE);
			return;
		}

		// PRINTING INPUTS
		listener.getLogger().println("instanceId : " + getInstanceId());
		listener.getLogger().println("url : " + getUrl());
		listener.getLogger().println("userId : " + getUserId());
		listener.getLogger().println("password : " + (getPassword().length()>4?getPassword().substring(0, 3) + "******":getPassword()));
		listener.getLogger().println("goalType : " + getGoalType());
		listener.getLogger().println("baseDir : " + getBaseDir());
		listener.getLogger().println("includeRule : " + getIncludeRule());
		listener.getLogger().println("excludeRule : " + getExcludeRule());
		listener.getLogger().println("srcLang : " + getSrcLang());
		listener.getLogger().println("type : " + getType());
		listener.getLogger().println("LangMap : " + getLangMap());
		listener.getLogger().println("Language Styling : " + getLanguageIdStyle());
		listener.getLogger().println("Download Source Lang? : " + getOutputSourceLang().toString());
		listener.getLogger().println("Download Content Option : " + getOutputContentOption());
		listener.getLogger().println("Bundle layout : " + getBundleLayout());
		listener.getLogger().println("Output Directory : " + getOutDir());
		listener.getLogger().println("Overwrite? : " + getOverwrite().toString());




		// All global variables
		JsonParser jsonParser = new JsonParser(); // JsonParser to parse langMap
		JsonElement parsedLangMap = null; // Parsed json element from string langMap
		JsonObject langMapObject = null; // Object represention of parsed langMap
		Set<String> langMapTargetLanguages = new HashSet<String>(); // fetching targetLanguages from langMap
		FilePath[] files = null; // files from baseDir after applying include/exclude rules
		Set<String> bundleIds;// = new HashSet<String>();
		Map<String, String> langMappingMap = new HashMap<String, String>();


		// CHECKING CREDENTIALS
		if(url.trim().equals("") || instanceId.trim().equals("") || userId.trim().equals("") || password.trim().equals("")){
			listener.getLogger().println("Empty credentials.. Please enter IBM Globalization Pipeline credentials (Instance id, url, username, password).");
			build.setResult(Result.UNSTABLE);
			return;
		}
		ServiceClient gpClient = null;
		try{
			gpClient = ServiceClient.getInstance(
					ServiceAccount.getInstance(
							url, instanceId,
							userId, password));
			gpClient.getBundleIds();
		}catch(ServiceException e){
			listener.getLogger().println("Invalid credentials.. Please enter valid IBM Globalization Pipeline credentials (Instance id, url, username, password).");
			build.setResult(Result.UNSTABLE);
			return;
		}



		// CHECKING PATHS
		if(baseDir.trim().equals("") || includeRule.trim().equals("")){
			listener.getLogger().println("Empty Base Directory or include/exclude rules. Please check again.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(!workspace.child(baseDir.trim()).exists()){
			listener.getLogger().println("Base Directory " + baseDir + " not present in workspace. Please check again.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(excludeRule.trim().equals(includeRule.trim())){
			listener.getLogger().println("Base Directory " + baseDir + " exists.. But you provided same include and exclude rule. i.e no files to consider.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		int numberOfIncludefiles = workspace.child(baseDir.trim()).list(includeRule.trim()).length;
		files = workspace.child(baseDir.trim()).list(includeRule, excludeRule);
		int numberOfFiles = files.length;
		if(numberOfIncludefiles == 0){
			listener.getLogger().println("Base Directory " + baseDir + " exists.. But include rules gives 0 files. i.e no files to consider.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		if(numberOfFiles == 0){
			listener.getLogger().println("Base Directory " + baseDir + " exists.. But exclude rule removed all files in include rule. i.e no files to consider.");
			build.setResult(Result.UNSTABLE);
			return;
		}
		listener.getLogger().println("Considering files ... with include rule:" + includeRule + " and exclude rule:" + excludeRule);
		listener.getLogger().println(printFilesConsidered(files));

		//CHECKING Source Language
		if(srcLang.trim().equals("")){
			listener.getLogger().println("Please enter source language");
			build.setResult(Result.UNSTABLE);
			return;
		}

		// CHECKING LANGUAGES AND MAPPING
		if(langMap.trim().equals("")){
			listener.getLogger().println("Please enter atleast one target language");
			build.setResult(Result.UNSTABLE);
			return;
		}
		try
		{
			parsedLangMap = jsonParser.parse(langMap.trim());
			langMapObject = parsedLangMap.getAsJsonObject();
			for(Entry<String, JsonElement> langMapEntry : langMapObject.entrySet()){
				langMapTargetLanguages.add(langMapEntry.getKey());
				if(langMapEntry.getValue().getAsString().equals("")){
					listener.getLogger().println("Please enter non-empty language mapping for target langugage: " + langMapEntry.getKey());
					build.setResult(Result.UNSTABLE);
					return;
				}
				langMappingMap.put(langMapEntry.getKey(), langMapEntry.getValue().getAsString());
			}
			if(langMapTargetLanguages.size() == 0){
				listener.getLogger().println("Please enter atleast one target language");
				build.setResult(Result.UNSTABLE);
				return;
			}


		}catch(JsonParseException jp){
			listener.getLogger().println("Invalid Json input of Language Map. Please enter valid json form");
			build.setResult(Result.UNSTABLE);
			return;
		}catch(Exception e){
			listener.getLogger().println("Exception error: " + e.getMessage());
			build.setResult(Result.FAILURE);
			return;
		}


		//CHECKING OUTPUT DIRECTORY
		if(outDir.trim().equals("")){
			listener.getLogger().println("Empty Output Directory. If you chose download goal, then this build will use/create ./target/classes in workspace");
		}
		if(!workspace.child(outDir.trim()).exists()){
			listener.getLogger().println("Output Directory does not exist in workspace. If you chose download goal, then this build will create " + outDir.trim() +" in workspace");
		}
		else if(!workspace.child(outDir.trim()).isDirectory()){
			listener.getLogger().println("Please enter Output Directory, not files");
			build.setResult(Result.FAILURE);
			return;
		}


		// UPLOAD
		if(goalType.equals("upload")){
			try {
				bundleIds = gpClient.getBundleIds();


				// Process each bundle
				for (FilePath bf : files) {


					// Checks if the bundle already exists
					String bundleId = pathToBundleId(type, bf);
					boolean createNew = false;
					if (bundleIds.contains(bundleId)) {
						listener.getLogger().println("Found Bundle: " + bundleId);

						// Checks if the source language matches.
						BundleData bundle = gpClient.getBundleInfo(bundleId);
						if (!srcLang.equals(bundle.getSourceLanguage())) {
							listener.getLogger().println("The source language in bundle:"
									+ bundleId + "(" + bundle.getSourceLanguage()
									+ ") does not match the specified language("
									+ srcLang + ").");
							build.setResult(Result.FAILURE);
							return;
						}
					} else {
						listener.getLogger().println("bundle:" + bundleId + " does not exist, creating a new bundle.");
						createNew = true;
					}
					
					
					// Parse the resource bundle file
					ResourceFilter filter = ResourceFilterFactory.getResourceFilter(getType());
					if (filter == null) {
						throw new IOException("Resource filter for " + getType() + " is not available.");
					}
					Map<String, NewResourceEntryData> resEntries = new HashMap<>();

					try (InputStream fis = bf.read()) {
						LanguageBundle resBundle = filter.parse(fis, new FilterOptions(Locale.forLanguageTag(srcLang)));

						if (createNew) {
							NewBundleData newBundleData = new NewBundleData(srcLang);
							// set target languages
							if (!langMapTargetLanguages.isEmpty()) {
								newBundleData.setTargetLanguages(new TreeSet<String>(langMapTargetLanguages));
							}
							// set bundle notes
							newBundleData.setNotes(resBundle.getNotes());
							gpClient.createBundle(bundleId, newBundleData);
							listener.getLogger().println("Created bundle: " + bundleId);
						}
						Collection<ResourceString> resStrings = resBundle.getResourceStrings();
						for (ResourceString resString : resStrings) {
							NewResourceEntryData resEntryData = new NewResourceEntryData(resString.getValue());
							int seqNum = resString.getSequenceNumber();
							if (seqNum >= 0) {
								resEntryData.setSequenceNumber(Integer.valueOf(seqNum));
							}
							// set resource string notes
							resEntryData.setNotes(resString.getNotes());
							resEntries.put(resString.getKey(), resEntryData);
						}
					} catch (IOException e) {
						listener.getLogger().println("Failed to read the resoruce data from "
								+ bf.toURI().getPath() + ": " + e.getMessage());
					} catch (ResourceFilterException e) {
						throw new IOException("Failed to parse the resource data from "
								+ bf.toURI().getPath() + ": " + e.getMessage(), e);
					}

					if (resEntries.isEmpty()) {
						listener.getLogger().println("No resource entries in " + bf.toURI().getPath());
					} else {
						// Upload the resource entries
						gpClient.uploadResourceEntries(bundleId, srcLang , resEntries);
						listener.getLogger().println("Uploaded source language(" + srcLang
								+ ") resource entries(" + resEntries.size() + ") to bundle: " + bundleId);
					}
				}

			} catch (ServiceException e) {
				listener.getLogger().println("Globalization Pipeline exception : " + e.getMessage());
				build.setResult(Result.UNSTABLE);
				return;
			}
		}



		// DOWNLOAD
		if(goalType.equals("download")){

			// IF outDir does not exist, create target/classes
			FilePath outDirectory = null;
			if (outDir == null) {
				outDirectory = new FilePath(workspace, "target/classes");
			}
			else if(outDir.trim().equals("")){
				listener.getLogger().println("Empty Output Directory. so checking if ./target/classes exists in workspace");
				outDirectory = new FilePath(workspace, "target/classes");
				if(!workspace.child("target/classes").exists()){
					listener.getLogger().println("./target/classes does not exists in workspace, so creating it");
					outDirectory.mkdirs();
				}
				else{
					listener.getLogger().println("./target/classes exists in workspace");
				}
			}
			else{
				outDirectory = new FilePath(workspace, outDir);
			}


			try {
				bundleIds = gpClient.getBundleIds();
			} catch (ServiceException e1) {
				listener.getLogger().println("Failed fetching bundles : " + e1.getMessage());
				build.setResult(Result.UNSTABLE);
				return;
			}
			// Process each bundle
			for (FilePath bf : files) {



				String bundleId = pathToBundleId(getType(), bf);
				if (!bundleIds.contains(bundleId)) {
					listener.getLogger().println("The bundle:" + bundleId + " does not exist.");
					continue;
				}

				BundleData bdlData = null;
				try {
					bdlData = gpClient.getBundleInfo(bundleId);
				} catch (ServiceException e) {
					listener.getLogger().println("Failed to get bundle data for " + bundleId + " : " + e.getMessage());
					build.setResult(Result.UNSTABLE);
					return;
				}

				String bdlSrcLang = bdlData.getSourceLanguage();
				Set<String> bdlTrgLangs = bdlData.getTargetLanguages();
				Set<String> bdlLangs = new HashSet<String>();
				bdlLangs.add(bdlSrcLang);
				if (bdlTrgLangs != null) {
					bdlLangs.addAll(bdlData.getTargetLanguages());
				}

				if (!srcLang.equals(bdlSrcLang)) {
					listener.getLogger().println("The source language of the bundle:" + bundleId
							+ " (" + bdlSrcLang + ") is different from the language specified by the configuration ("
							+ srcLang + ")");

				}

				if (outputSourceLang) {
					if (bdlLangs.contains(srcLang)) {
						try {
							exportLanguageResource(gpClient, bf, srcLang, outDirectory,
									outputContentOption, bundleLayout , languageIdStyle, srcLang, langMappingMap, listener);
						} catch (ServiceException e) {
							listener.getLogger().println("Failed to export language resource " + bundleId + " : " + e.getMessage());
							build.setResult(Result.UNSTABLE);
							return;
						}
					} else {
						listener.getLogger().println("The specified source language (" + srcLang
								+ ") does not exist in the bundle:" + bundleId);
					}
				}

				for (String tgtLang: langMapTargetLanguages) {
					if (bdlLangs.contains(tgtLang)) {
						try {
							exportLanguageResource(gpClient, bf, tgtLang, outDirectory,
									outputContentOption, bundleLayout, languageIdStyle, srcLang, langMappingMap, listener);
						} catch (ServiceException e) {
							listener.getLogger().println("Failed to export language resource " + bundleId + " : " + e.getMessage());
							build.setResult(Result.UNSTABLE);
							return;
						}
					} else {
						listener.getLogger().println("The specified target language (" + tgtLang
								+ ") does not exist in the bundle:" + bundleId);
					}
				}

			}


		}


		listener.getLogger().println("*************** IBM GLOBALIZATION PIPELINE BUILDSTEP Done!! ***************");

	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}



	/**
	 * Descriptor for {@link GlobalizationPipelineBuilder}. Used as a singleton.
	 * The class is marked as public so that it can be accessed from views.
	 *
	 * <p>
	 * See {@code src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly}
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use {@code transient}.
		 */


		/**
		 * In order to load the persisted global configuration, you have to 
		 * call load() in the constructor.
		 */
		public DescriptorImpl() {
			load();
		}


		// VALIDATING INSTANCEID
		public FormValidation doCheckInstanceId(@QueryParameter("instanceId") String instanceId)throws IOException, ServletException{

			try{
				if(instanceId == null){
					return FormValidation.error("null INSTANCEID :/");
				}
				if(instanceId.trim().equals("")){
					return FormValidation.error("Please enter INSTANCEID of Globalization Pipeline instance.. See (?) button for more details");
				}
				return FormValidation.okWithMarkup("<span style='color:green'>Please test connection after filling instanceId, username, passoword and url.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @instanceId " + e.getMessage());
			}

		}

		// VALIDATING URL
		public FormValidation doCheckUrl(@QueryParameter("url") String url)throws IOException, ServletException{

			try{
				if(url == null){
					return FormValidation.error("null URL :/");
				}
				if(url.trim().equals("")){
					return FormValidation.error("Please enter URL of Globalization Pipeline instance.. See (?) button for more details");
				}
				//return FormValidation.okWithMarkup("Please test connection after filling instanceId, username, passoword and url");
				new URL(url.trim());
				return FormValidation.okWithMarkup("<span style='color:green'> is syntactically correct. Please verify connection after filling instanceId, username, passoword and url</span>");
			}catch (MalformedURLException e) {
				return FormValidation.error("Invalid url. Please check it again. May be missing http(s)://");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @url : " + e.getMessage());
			}

		}

		// VALIDATING USERID
		public FormValidation doCheckUserId(@QueryParameter("userId") String userId)throws IOException, ServletException{

			try{
				if(userId == null){
					return FormValidation.error("null USERID :/");
				}
				if(userId.trim().equals("")){
					return FormValidation.error("Please enter USERID of Globalization Pipeline instance.. See (?) button for more details");
				}
				return FormValidation.okWithMarkup("<span style='color:green'>Seems ok. Remember to test connection though.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @userId " + e.getMessage());
			}

		}

		// VALIDATING PASSWORD
		public FormValidation doCheckPassword(@QueryParameter("password") String password)throws IOException, ServletException{

			try{
				if(password == null){
					return FormValidation.error("null PASSWORD :/");
				}
				if(password.trim().equals("")){
					return FormValidation.error("Please enter PASSWORD of Globalization Pipeline instance.. See (?) button for more details");
				}
				return FormValidation.okWithMarkup("<span style='color:green'>Seems ok. Remember to test connection though.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @password " + e.getMessage());
			}

		}

		// TESTING GP CONNECTION
		public FormValidation doTestConnection(@QueryParameter("instanceId") String instanceId,
				@QueryParameter("url") String url, @QueryParameter("userId") String userId,
				@QueryParameter("password") String password) throws IOException, ServletException, ServiceException {
			try {

				if(url == null || instanceId == null || userId == null || password == null){
					return FormValidation.error("One/more of the previous field(s) is/are null");
				}

				if(url.trim().equals("") || instanceId.trim().equals("") || userId.trim().equals("") || password.trim().equals("")){
					return FormValidation.error("One/more of the previous field(s) is/are empty");
				}

				ServiceClient gpClient = ServiceClient.getInstance(
						ServiceAccount.getInstance(
								url, instanceId,
								userId, password));
				gpClient.getBundleIds();
				return FormValidation.okWithMarkup("<b><span style='color:green'>Successful connection!</span></b>");
			} catch (Exception e) {
				return FormValidation.error("Globalization Pipeline error : Please check credentials.. Make sure you input credential from Globalization Pipeline instance on IBM Bluemix");
			}
		}

		//VALIDATING GOALTYPE - NOT NEEDED

		// VALIDATING BASEDIR
		@SuppressWarnings("deprecation")
		public FormValidation doCheckBaseDir(@QueryParameter("baseDir") String baseDir, @AncestorInPath AbstractProject project)throws IOException, ServletException{

			try{
				if(baseDir == null){
					return FormValidation.error("null BASE DIRECTORY :/");
				}
				if(baseDir.trim().equals("")){
					return FormValidation.error("Please input Base Directory (realtive path in workspace) that has resource files");
				}
				if(project.getWorkspace() == null){
					return FormValidation.error("No workspace exist??");
				}
				FilePath work = project.getWorkspace();
				if(!work.child(baseDir.trim()).exists()){
					return FormValidation.warning("No such path exist in your workspace at the moment. Make sure you pull this Base Directory on workspace from previous build steps");
				}
				return FormValidation.okWithMarkup("<span style='color:green'><b>" + baseDir +"</b> exists in workspace!.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @baseDirectory : " + e.getMessage());
			}

		}


		// VALIDATING INCLUDERULES
		@SuppressWarnings("deprecation")
		public FormValidation doCheckIncludeRule(@QueryParameter("includeRule") String includeRule, @AncestorInPath AbstractProject project, @QueryParameter("baseDir") String baseDir)throws IOException, ServletException{

			try{
				if(baseDir == null){
					return FormValidation.error("null BASE DIRECTORY :/");
				}
				if(baseDir.trim().equals("")){
					return FormValidation.error("Please input Base Directory (relative path in workspace) in previous field that has resource files");
				}
				if(includeRule == null){
					return FormValidation.error("null INCLUDE RULE :/");
				}
				if(includeRule.trim().equals("")){
					return FormValidation.error("Please input Ant-Style include Rule to include all resource file(s)/bundle(s)");
				}
				if(project.getWorkspace() == null){
					return FormValidation.error("No workspace exist??");
				}
				FilePath work = project.getWorkspace();
				if(!work.child(baseDir.trim()).exists()){
					return FormValidation.warning("Base Directory you mentioned does not exist in workspace at the moment. Assuming that Base Directory will be pulled, make sure all the files mentioned in Include Rule exist in your SCM");
				}
				int files = work.child(baseDir.trim()).list(includeRule).length;
				if(files == 0){
					return FormValidation.warning("Base Directory exists, but no such files exist in workspace at the moment. Make sure those files are pulled in previous build steps");
				}
				return FormValidation.okWithMarkup("<span style='color:green'><b>" + files +"</b> match(es) found in current workspace with include rule!.</span>");
			}catch(Exception e){
				return FormValidation.error("Include Rule format issue : " + e.getMessage());
			}

		}

		// VALIDATING EXCLUDERULES
		@SuppressWarnings("deprecation")
		public FormValidation doCheckExcludeRule(@QueryParameter("excludeRule") String excludeRule, 
				@AncestorInPath AbstractProject project, @QueryParameter("baseDir") String baseDir,
				@QueryParameter("includeRule") String includeRule)throws IOException, ServletException{

			try{
				if(baseDir == null){
					return FormValidation.error("null BASE DIRECTORY :/");
				}
				if(baseDir.trim().equals("")){
					return FormValidation.error("Please input Base Directory (relative path in workspace) in previous field that has resource files");
				}
				if(project.getWorkspace() == null){
					return FormValidation.error("No workspace exist??");
				}
				FilePath work = project.getWorkspace();
				if(!work.child(baseDir.trim()).exists()){
					return FormValidation.warning("Base Directory you mentioned does not exist in workspace at the moment. Assuming that Base Directory will be pulled, make sure all the files mentioned in Exclude Rule exist in your SCM");
				}
				if(includeRule == null){
					return FormValidation.error("null INCLUDE RULE :/");
				}
				if(includeRule.trim().equals("")){
					return FormValidation.error("Please input Ant-Style include Rule in previous field to include all resource file(s)/bundle(s)");
				}
				if(excludeRule == null){
					return FormValidation.error("null EXCLUDE RULE :/");
				}
				if(excludeRule.trim().equals("")){
					return FormValidation.okWithMarkup("<span style='color:green'>No file(s)/bundle(s) will be excluded</span>");
				}
				if(excludeRule.trim().equals(includeRule.trim())){
					return FormValidation.error("Same include and exclude rule.. so, no files/bundles can be processed");
				}
				int includefiles = work.child(baseDir.trim()).list(includeRule).length;
				int excludefiles = work.child(baseDir.trim()).list(excludeRule).length;
				int files = work.child(baseDir.trim()).list(includeRule, excludeRule).length;
				if(excludefiles == 0){
					return FormValidation.warning("Base Directory exists, but no such files exist in workspace at the moment. Make sure those files are pulled in previous build steps");
				}

				return FormValidation.okWithMarkup("<span style='color:green'><b>" + includefiles + "</b> match(es) found in current workspace with include rule!.<br/><b>"
						+ (excludefiles) + "</b> match(es) found in current workspace with exclude rule!.<br/><b>"
						+ files + "</b> files/bundles will be processed in current workspace with given include and exclude rule!. Results will vary depending on SCM pull to workspace from previous build step</span>");
			}catch(Exception e){
				return FormValidation.error("Include Rule format issue : " + e.getMessage());
			}

		}


		// VALIDATING SOURCE LANGUAGE
		public FormValidation doCheckSrcLang(@QueryParameter("srcLang") String srcLang)throws IOException, ServletException{

			try{
				if(srcLang == null){
					return FormValidation.error("null SOURCE LANGUAGE :/");
				}
				if(srcLang.trim().equals("")){
					return FormValidation.errorWithMarkup("Please enter SOURCE LANGUAGE of source files. for eg. <b>en</b>.");
				}
				return FormValidation.okWithMarkup("<span style='color:green'>Please make sure source Language is valid BCP47. We do not validate it here.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @srcLang " + e.getMessage());
			}

		}

		// VALIDATING LangMAP
		public FormValidation doCheckLangMap(@QueryParameter("langMap") String langMap,
				@QueryParameter("instanceId") String instanceId,
				@QueryParameter("url") String url, @QueryParameter("userId") String userId,
				@QueryParameter("password") String password)throws IOException, ServletException, JsonSyntaxException{

			try{
				if(langMap == null){
					return FormValidation.error("null LANGUAGE MAPPING :/");
				}
				if(langMap.trim().equals("")){
					return FormValidation.error("Please enter atleast one target language :/");
				}
				if(url == null || instanceId == null || userId == null || password == null){
					return FormValidation.error("One/more of the Credential field(s) is/are null");
				}

				if(url.trim().equals("") || instanceId.trim().equals("") || userId.trim().equals("") || password.trim().equals("")){
					return FormValidation.error("One/more of the Credential field(s) is/are empty");
				}
				ServiceClient gpClient;
				try{
					gpClient = ServiceClient.getInstance(
							ServiceAccount.getInstance(
									url, instanceId,
									userId, password));
					gpClient.getBundleIds();
				}catch(Exception ge){
					return FormValidation.error("Please enter the correct credentials for Globalization Pipeline");
				}

				JsonParser jsonParser = new JsonParser();
				JsonElement parsedLangMap = jsonParser.parse(langMap.trim());
				JsonObject langMapObject = parsedLangMap.getAsJsonObject();
				if(langMapObject.entrySet().size() == 0){
					return FormValidation.error("Please enter atleast one target language :/");
				}
				return FormValidation.okWithMarkup("<span style='color:green'>Please make sure that keys are well formed BCP47 of target Languages. We do not validate it here. </span>");


			}catch(JsonParseException jp){
				return FormValidation.error("Invalid Json input of Language Map. Please enter valid json form");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @language Map " + e.getMessage());
			}
		}



		// VALIDATING OutputSourceLang NO NEED
		// VALIDATING OutputCotentOption NO NEED
		// VALIDATING BundleLayout NO NEED


		// VALIDATING OUTDIR
		@SuppressWarnings("deprecation")
		public FormValidation doCheckOutDir(@QueryParameter("outDir") String outDir, @AncestorInPath AbstractProject project)throws IOException, ServletException{

			try{
				if(outDir == null){
					return FormValidation.error("null OUTPUT DIRECTORY :/");
				}
				if(outDir.trim().equals("")){
					return FormValidation.warning("No directory specified. By default, download goal will download content in './target/classes'");
				}
				if(project.getWorkspace() == null){
					return FormValidation.error("No workspace exist??");
				}
				FilePath work = project.getWorkspace();
				if(!work.child(outDir.trim()).exists()){
					return FormValidation.warning("No such path exist in your workspace at the moment. Either pull this Output Directory on workspace from previous build steps OR this build will create it in workspace");
				}
				else if(!work.child(outDir.trim()).isDirectory()){
					return FormValidation.error("Please enter DIRECTORY, not files");
				}
				return FormValidation.okWithMarkup("<span style='color:green'><b>" + outDir +"</b> exists in workspace!.</span>");
			}catch(Exception e){
				return FormValidation.error("Something went wrong @baseDirectory : " + e.getMessage());
			}

		}


		// VALIDATING Overwrite NO NEED



		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			// Indicates that this builder can be used with all kinds of project types 
			return true;
		}

		/**
		 * This human readable name is used in the configuration screen.
		 */
		public String getDisplayName() {
			return "IBM Globalization Pipeline \uD83C\uDF10";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			// ^Can also use req.bindJSON(this, formData);
			//  (easier when there are many fields; need set* methods for this)
			save();
			return super.configure(req,formData);
		}

	}
}

