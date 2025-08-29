
def f = (this.args.length>0) ? this.args[0] : "Test Recipe for Export.ACBK"
decompile(f)

def decompile(String f) {
	def is = new FileInputStream(f)
	def adr = 0;
	is.eachByte { b ->
		if (adr % 16 == 0) {
			println()
			printf("%04x ", adr)
		}
		char c = b
		def str = ""
		while (b>=32 && b<=122) {
			str += c
			b = is.read()
			c = b
			adr++
		}
		if (str.size()>0) {
			println(" "+str);
			printf("%04x ", adr)
		} else if (b == 0x86) {
			println()
			printf("%04x ", adr)
		}
		printf "%02x ", b
		adr++
	}
}
