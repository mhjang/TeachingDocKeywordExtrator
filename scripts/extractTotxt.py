import os, sys
	
def main():
	files = [f for f in os.listdir(sys.argv[1])]
	newdir = sys.argv[1] + "/extracted/"
	if not os.path.exists(newdir):	
		os.system("mkdir " + sys.argv[1]+"/extracted")
	for f in files:
        	if (f.endswith('.html') or f.endswith('.pdf') or f.endswith('.pptx') or f.endswith('.ppt')):
			os.system("java -jar tika-app-1.4.jar -t "+sys.argv[1]+f + " > " + newdir+f+".txt")
			print f, " extracted"
			print "java -jar tika-app-1.4.jar -t "+f + " > " + newdir+f+".txt"

if __name__ == "__main__":
	if len(sys.argv) < 2:
		print "usage: python extractTotxt.py --inputDir"
	else: 
		main()
