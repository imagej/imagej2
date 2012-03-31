#ifdef __amd64__
   #define GLIBC_COMPAT_SYMBOL(FFF) __asm__(".symver " #FFF "," #FFF "@GLIBC_2.2.5")
#else
   #define GLIBC_COMPAT_SYMBOL(FFF) __asm__(".symver " #FFF "," #FFF "@GLIBC_2.0")
#endif /*__amd64__*/

GLIBC_COMPAT_SYMBOL(fclose);
GLIBC_COMPAT_SYMBOL(fopen);
GLIBC_COMPAT_SYMBOL(dlopen);
GLIBC_COMPAT_SYMBOL(in6addr_loopback);
GLIBC_COMPAT_SYMBOL(__stack_chk_fail);
