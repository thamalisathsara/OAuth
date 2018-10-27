var totalFileLength, totalFileUploaded, fileCount, filesUploaded;

// To log everything on console
function debug(s) {
	var debug = document.getElementById('debug');
	if (debug) {
		debug.innerHTML = debug.innerHTML + '<br/>' + s;
	}
}

// Will be called when upload is completed
function onUploadComplete(e) {
	totalFileUploaded += document.getElementById('files').files[filesUploaded].size;
	filesUploaded++;
	debug('complete ' + filesUploaded + " of " + fileCount);
	debug('totalFileUploaded: ' + totalFileUploaded);
	if (filesUploaded < fileCount) {
		uploadNext();
	} else {
		var bar = document.getElementById('bar');
		bar.style.width = '100%';
		bar.innerHTML = '100 % complete';
		bootbox.alert('Finished uploading file(s)');

	}
}

// Will be called when user select the files in file control
function onFileSelect(e) {
	var files = e.target.files; 
	var output = [];
	fileCount = files.length;
	totalFileLength = 0;
	for (var i = 0; i < fileCount; i++) {
		var file = files[i];
		output.push(file.name, ' (', file.size, ' bytes, ',
				file.lastModifiedDate.toLocaleDateString(), ')');
		output.push('<br/>');
		debug('add ' + file.size);
		totalFileLength += file.size;
	}
	document.getElementById('selectedFiles').innerHTML = output.join('');
}

// This will continuously update the progress bar based on the percentage of image uploaded
function onUploadProgress(e) {
	if (e.lengthComputable) {
		var percentComplete = parseInt((e.loaded + totalFileUploaded) * 100 / totalFileLength);
		
		if(percentComplete>100)
			percentComplete = 100;
		var bar = document.getElementById('bar');
		bar.style.width = percentComplete + '%';
		bar.innerHTML = percentComplete + ' % complete';
	} else {
		debug('unable to compute');
	}
}

// Any other errors in uploading files will be handled here.
function onUploadFailed(e) {
	bootbox.alert("Error uploading file");
}

// Pick the next file in queue and upload it to remote server
function uploadNext() {
	var xhr = new XMLHttpRequest();
	var fd = new FormData();
	var file = document.getElementById('files').files[filesUploaded];
	fd.append("multipartFile", file);
	xhr.upload.addEventListener("progress", onUploadProgress, false);
	xhr.addEventListener("load", onUploadComplete, false);
	xhr.addEventListener("error", onUploadFailed, false);
	xhr.open("POST", "upload");
    console.log("fd", file);
xhr.send(fd);
}

function nullValidation(){
	var file = document.getElementById("files").files;
	var length = file.length;
	
	if(length <= 0) {
		
		return false;
	}
	else {
		return true;
	}			
}

////Validate each file type before uploading  
//function validateFileFormat(){
//	var files = document.getElementById("files").files;
//	var val = true;
//	for (let i = 0; i < files.length; i++){
//		var fileInput = files[i];
//		var fileType = fileInput.name;
////		var allowedExtensions = /(\.jpg|\.jpeg|\.png|\.gif|\.bmp)$/i;
//		var allowedExtensions = /(\.mp4|\.mov)$/i;
//	    if(!allowedExtensions.exec(fileType)){
//	    	val = false;
//	    	break;
//	    }
//	}
//	return val;
//}

// Let's begin the upload process
function startUpload() {
	if(!nullValidation()){
		bootbox.alert("Please select an image to upload!");
	}
	else{
//		if(!validateFileFormat()){
//			bootbox.alert("Please select only image files!");
//		}else{
			totalFileUploaded = filesUploaded = 0;
			uploadNext();
//		}
	}	
}

function resetAll(){
	document.getElementById("imageUpload").reset();
	document.getElementById("selectedFiles").value=" ";
	var bar = document.getElementById('bar');
	bar.style.width = 0;
	bar.innerHTML = " ";

}

// Event listeners for button clicks
window.onload = function() {
	document.getElementById('files').addEventListener('change', onFileSelect, false);
	document.getElementById('uploadButton').addEventListener('click', startUpload, false);
	document.getElementById('resetButton').addEventListener('click', resetAll, false);
	
}

