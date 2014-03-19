#################################################################
# Written in 2/17/2014 4:16pm 
# This script is to convert teaching documents in pdf, html, etc forms to text files using Tika open-source library.
# It simply iterates the given directory, and makes a library call to convert the file. 
# Because it takes the input file argument as system argument, the directory name SHOULD NOT have any whitespace.
# ################################################################



import os as os 
import sys
from os.path import isfile, isdir, join

# command line example 
# java -jar ~/Downloads/tika-app-1.4.jar -t /Users/mhjang/Downloads/documents-export-2014-02-17/Computability\ Theory/MIT6_045JS11_lec07.pdf > /Users/mhjang/Downloads/documents-export-2014-02-17/Computability\ Theory/MIT6_045JS11_lec07.pdf.txt

# takes file directory path and tika jar file path 
def convertFiles(path, tikapath):
	files = os.listdir(path)
	for file in files:
		filepath = join(path, file)
		if isdir(filepath):
			convertFiles(filepath, tikapath)
		elif isfile(filepath):
			os.system("java -jar " + tikapath + " -t " + filepath + " > " + filepath+".txt")
			print("java -jar " + tikapath + " -t " + filepath + " > " + filepath+".txt")

def main():
	if len(sys.argv) < 3:
		print("python convertDocs.py [input file directory] [tika library app directory]")
		print("tika path: /Users/mhjang/Documents/workspace/TeachingTest/library/tika-app-1.4.jar")
	else:
		convertFiles(sys.argv[1], sys.argv[2])
	print("successfully converted!")

if __name__ == "__main__":
	main()

