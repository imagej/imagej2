#!/bin/sh

cd "$(dirname "$0")"

header_file=src/main/c/glibc-compat.h
license_header="$(sed -n '1,/^$/p' < $header_file)"

symbols=
if test -f $header_file
then
	symbols="$(echo; grep ^GLIBC < $header_file)"
fi

# From http://www.trevorpounds.com/blog/?p=103

cat << EOF > $header_file
$license_header

#ifdef __amd64__
   #define GLIBC_COMPAT_SYMBOL(FFF) __asm__(".symver " #FFF "," #FFF "@GLIBC_2.2.5")
#else
   #define GLIBC_COMPAT_SYMBOL(FFF) __asm__(".symver " #FFF "," #FFF "@GLIBC_2.0")
#endif /*__amd64__*/
$symbols
EOF

case "$(uname -m)" in
x86_64)
	arch=amd64
	version_regex='2\.2\.5'
	;;
*)
	arch=i386
	version_regex='2\.0'
	;;
esac
launcher_version="$(sed -n 's/.*<version>\([^<]*\).*/\1/p' < pom.xml | head -n 1)"
launcher_path="target/nar/ij-launcher-$launcher_version-$arch-Linux-gcc-executable/bin/$arch-Linux-gcc/ij-launcher"
objdump -t "$launcher_path" |
grep -v -e "GLIBC_$version_regex" |
sed -n -e 's/.* \([^ ]*\)\@\@\(GLIBC_.*\)/GLIBC_COMPAT_SYMBOL(\1);/p' \
>> $header_file
