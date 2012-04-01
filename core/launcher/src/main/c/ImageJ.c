/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

/*
 * This is the ImageJ launcher, a small native program to handle the
 * startup of Java and ImageJ.
 *
 * This program was originally developed as the Fiji launcher
 * (http://fiji.sc/), but has been adapted and improved for use with ImageJ
 * core.
 *
 * The Fiji launcher is copyright 2007 - 2011 Johannes Schindelin, Mark
 * Longair, Albert Cardona, Benjamin Schmid, Erwin Frise and Gregory Jefferis.
 *
 * Clarification: the license of the ImageJ launcher has no effect on
 * the Java Runtime, ImageJ or any plugins, since they are not derivatives.
 *
 * @author Johannes Schindelin
 */

#define _BSD_SOURCE
#include <stdlib.h>
#include "jni.h"
#include <stdlib.h>
#include <limits.h>
#include <string.h>

#if defined(_WIN64) && !defined(WIN32)
/* TinyCC's stdlib.h undefines WIN32 in 64-bit mode */
#define WIN32 1
#endif

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#if !defined(WIN32) || !defined(__TINYC__)
#include <unistd.h>
#endif
#include <errno.h>

#ifdef __GNUC__
#define MAYBE_UNUSED __attribute__ ((unused))
#else
#define MAYBE_UNUSED
#endif

#ifdef __APPLE__
#include <stdlib.h>
#include <pthread.h>
#include <CoreFoundation/CoreFoundation.h>
#include <ApplicationServices/ApplicationServices.h>

struct string;
static void append_icon_path(struct string *str);
static void set_path_to_JVM(void);
static int get_fiji_bundle_variable(const char *key, struct string *value);
#endif

static const char *get_platform(void)
{
#ifdef __APPLE__
	return "macosx";
#endif
#ifdef WIN32
	return sizeof(void *) < 8 ? "win32" : "win64";
#endif
#ifdef __linux__
	return sizeof(void *) < 8 ? "linux32" : "linux64";
#endif
	return NULL;
}

#ifdef WIN32
#include <io.h>
#include <process.h>
#define PATH_SEP ";"

static void open_win_console();
static void win_error(const char *fmt, ...);
static void win_verror(const char *fmt, va_list ap);

/* TODO: use dup2() and freopen() and a thread to handle the output */
#else
#define PATH_SEP ":"
#ifndef O_BINARY
#define O_BINARY 0
#endif
#endif

#if defined(__linux__) && !defined(__TINYC__)
#include "glibc-compat.h"
#endif

__attribute__((format (printf, 1, 2)))
static void error(const char *fmt, ...)
{
	va_list ap;
#ifdef WIN32
	const char *debug = getenv("WINDEBUG");
	if (debug && *debug) {
		va_start(ap, fmt);
		win_verror(fmt, ap);
		va_end(ap);
		return;
	}
	open_win_console();
#endif

	va_start(ap, fmt);
	vfprintf(stderr, fmt, ap);
	va_end(ap);
	fputc('\n', stderr);
}

__attribute__((__noreturn__))
__attribute__((format (printf, 1, 2)))
static void die(const char *fmt, ...)
{
	va_list ap;

	va_start(ap, fmt);
	vfprintf(stderr, fmt, ap);
	va_end(ap);
	fputc('\n', stderr);

	exit(1);
}

static void *xmalloc(size_t size)
{
	void *result = malloc(size);
	if (!result)
		die("Out of memory");
	return result;
}

static void *xcalloc(size_t size, size_t nmemb)
{
	void *result = calloc(size, nmemb);
	if (!result)
		die("Out of memory");
	return result;
}

static void *xrealloc(void *p, size_t size)
{
	void *result = realloc(p, size);
	if (!result)
		die("Out of memory");
	return result;
}

static char *xstrdup(const char *buffer)
{
	char *result = strdup(buffer);
	if (!result)
		die("Out of memory");
	return result;
}

static char *xstrndup(const char *buffer, size_t max_length)
{
	char *eos = memchr(buffer, '\0', max_length - 1);
	int len = eos ? eos - buffer : max_length;
	char *result = xmalloc(len + 1);

	if (!result)
		die("Out of memory");

	memcpy(result, buffer, len);
	result[len] = '\0';

	return result;
}

struct string {
	int alloc, length;
	char *buffer;
};

static void string_ensure_alloc(struct string *string, int length)
{
	if (string->alloc <= length) {
		char *new_buffer = xrealloc(string->buffer, length + 1);

		string->buffer = new_buffer;
		string->alloc = length;
	}
}

static void string_set_length(struct string *string, int length)
{
	if (length == string->length)
		return;
	if (length > string->length)
		die("Cannot enlarge strings");
	string->length = length;
	string->buffer[length] = '\0';
}

static void string_set(struct string *string, const char *buffer)
{
	free(string->buffer);
	string->buffer = xstrdup(buffer);
	string->alloc = string->length = strlen(buffer);
}

static struct string *string_init(int length)
{
	struct string *string = xcalloc(sizeof(struct string), 1);

	string_ensure_alloc(string, length);
	string->buffer[0] = '\0';
	return string;
}

static struct string *string_copy(const char *string)
{
	int len = strlen(string);
	struct string *result = string_init(len);

	memcpy(result->buffer, string, len + 1);
	result->length = len;

	return result;
}

static void string_release(struct string *string)
{
	if (string) {
		free(string->buffer);
		free(string);
	}
}

static void string_add_char(struct string *string, char c)
{
	if (string->alloc == string->length)
		string_ensure_alloc(string, 3 * (string->alloc + 16) / 2);
	string->buffer[string->length++] = c;
	string->buffer[string->length] = '\0';
}

static void string_append(struct string *string, const char *append)
{
	int len = strlen(append);

	string_ensure_alloc(string, string->length + len);
	memcpy(string->buffer + string->length, append, len + 1);
	string->length += len;
}

static int path_list_contains(const char *list, const char *path)
{
	size_t len = strlen(path);
	const char *p = list;
	while (p && *p) {
		if (!strncmp(p, path, len) &&
				(p[len] == PATH_SEP[0] || !p[len]))
			return 1;
		p = strchr(p, PATH_SEP[0]);
		if (!p)
			break;
		p++;
	}
	return 0;
}

static void string_append_path_list(struct string *string, const char *append)
{
	if (!append || path_list_contains(string->buffer, append))
		return;

	if (string->length)
		string_append(string, PATH_SEP);
	string_append(string, append);
}

static void string_append_at_most(struct string *string, const char *append, int length)
{
	int len = strlen(append);

	if (len > length)
		len = length;

	string_ensure_alloc(string, string->length + len);
	memcpy(string->buffer + string->length, append, len + 1);
	string->length += len;
	string->buffer[string->length] = '\0';
}

static void string_replace_range(struct string *string, int start, int end, const char *replacement)
{
	int length = strlen(replacement);
	int total_length = string->length + length - (end - start);

	if (end != start + length) {
		string_ensure_alloc(string, total_length);
		if (string->length > end)
			memmove(string->buffer + start + length, string->buffer + end, string->length - end);
	}

	if (length)
		memcpy(string->buffer + start, replacement, length);
	string->buffer[total_length] = '\0';
	string->length = total_length;
}

static int number_length(unsigned long number, long base)
{
        int length = 1;
        while (number >= base) {
                number /= base;
                length++;
        }
        return length;
}

static inline int is_digit(char c)
{
	return c >= '0' && c <= '9';
}

static void string_vaddf(struct string *string, const char *fmt, va_list ap)
{
	while (*fmt) {
		char fill = '\0';
		int size = -1, max_size = -1;
		char *p = (char *)fmt;

		if (*p != '%' || *(++p) == '%') {
			string_add_char(string, *p++);
			fmt = p;
			continue;
		}
		if (*p == ' ' || *p == '0')
			fill = *p++;
		if (is_digit(*p))
			size = (int)strtol(p, &p, 10);
		else if (p[0] == '.' && p[1] == '*') {
			max_size = va_arg(ap, int);
			p += 2;
		}
		switch (*p) {
		case 's': {
			const char *s = va_arg(ap, const char *);
			if (fill) {
				int len = size - strlen(s);
				while (len-- > 0)
					string_add_char(string, fill);
			}
			while (*s && max_size--)
				string_add_char(string, *s++);
			break;
		}
		case 'c':
			{
				char c = va_arg(ap, int);
				string_add_char(string, c);
			}
			break;
		case 'u':
		case 'i':
		case 'l':
		case 'd':
		case 'o':
		case 'x':
		case 'X': {
			int base = *p == 'x' || *p == 'X' ? 16 :
				*p == 'o' ? 8 : 10;
			int negative = 0, len;
			unsigned long number, power;

			if (*p == 'u') {
				number = va_arg(ap, unsigned int);
			}
			else {
				long signed_number;
				if (*p == 'l') {
					signed_number = va_arg(ap, long);
				}
				else {
					signed_number = va_arg(ap, int);
				}
				if (signed_number < 0) {
					negative = 1;
					number = -signed_number;
				} else
					number = signed_number;
			}

			/* pad */
			len = number_length(number, base);
			while (size-- > len + negative)
				string_add_char(string, fill ? fill : ' ');
			if (negative)
				string_add_char(string, '-');

			/* output number */
			power = 1;
			while (len-- > 1)
				power *= base;
			while (power) {
				int digit = number / power;
				string_add_char(string, digit < 10 ? '0' + digit
					: *p + 'A' - 'X' + digit - 10);
				number -= digit * power;
				power /= base;
			}

			break;
		}
		default:
			/* unknown / invalid format: copy verbatim */
			string_append_at_most(string, fmt, p - fmt + 1);
		}
		fmt = p + (*p != '\0');
	}
}

__attribute__((format (printf, 2, 3)))
static void string_addf(struct string *string, const char *fmt, ...)
{
	va_list ap;

	va_start(ap, fmt);
	string_vaddf(string, fmt, ap);
	va_end(ap);
}

__attribute__((format (printf, 2, 3)))
static void string_setf(struct string *string, const char *fmt, ...)
{
	va_list ap;

	string_ensure_alloc(string, strlen(fmt) + 64);
	string->length = 0;
	string->buffer[0] = '\0';
	va_start(ap, fmt);
	string_vaddf(string, fmt, ap);
	va_end(ap);
}

__attribute__((format (printf, 1, 2)))
static struct string *string_initf(const char *fmt, ...)
{
	struct string *string = string_init(strlen(fmt) + 64);
	va_list ap;

	va_start(ap, fmt);
	string_vaddf(string, fmt, ap);
	va_end(ap);

	return string;
}

static void string_replace(struct string *string, char from, char to)
{
	int j;
	for (j = 0; j < string->length; j++)
		if (string->buffer[j] == from)
			string->buffer[j] = to;
}

static MAYBE_UNUSED int string_read_file(struct string *string, const char *path) {
	FILE *file = fopen(path, "rb");
	char buffer[1024];
	int result = 0;

	if (!file) {
		error("Could not open %s", path);
		return -1;
	}

	for (;;) {
		size_t count = fread(buffer, 1, sizeof(buffer), file);
		string_ensure_alloc(string, string->length + count);
		memcpy(string->buffer + string->length, buffer, count);
		string->length += count;
		if (count < sizeof(buffer))
			break;
	}
	if (ferror(file) < 0)
		result = -1;
	fclose(file);
	return result;
}

static void string_escape(struct string *string, const char *characters)
{
	int i, j = string->length;

	for (i = 0; i < string->length; i++)
		if (strchr(characters, string->buffer[i]))
			j++;
	if (i == j)
		return;
	string_ensure_alloc(string, j);
	string->buffer[j] = '\0';
	while (--i < --j) {
		string->buffer[j] = string->buffer[i];
		if (strchr(characters, string->buffer[j]))
			string->buffer[--j] = '\\';
	}
}

/*
 * If set, overrides the environment variable JAVA_HOME, which in turn
 * overrides relative_java_home.
 */
static const char *absolute_java_home;
static const char *relative_java_home;
static const char *default_library_path;
static const char *library_path;
static const char *default_fiji1_class = "fiji.Main";
static const char *default_main_class = "imagej.Main";

static int is_default_ij1_class(const char *name)
{
	return name && (!strcmp(name, default_fiji1_class) || !strcmp(name, "ij.ImageJ"));
}

/* Dynamic library loading stuff */

#ifdef WIN32
#include <windows.h>
#define RTLD_LAZY 0
static char *dlerror_value;

static char *get_win_error(void)
{
	DWORD error_code = GetLastError();
	LPSTR buffer;

	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
			FORMAT_MESSAGE_FROM_SYSTEM |
			FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL,
			error_code,
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
			(LPSTR)&buffer,
			0, NULL);
	return buffer;
}

static void *dlopen(const char *name, int flags)
{
	void *result = LoadLibrary(name);

	dlerror_value = get_win_error();

	return result;
}

static char *dlerror(void)
{
	/* We need to reset the error */
	char *result = dlerror_value;
	dlerror_value = NULL;
	return result;
}

static void *dlsym(void *handle, const char *name)
{
	void *result = (void *)GetProcAddress((HMODULE)handle, name);
	dlerror_value = result ? NULL : (char *)"function not found";
	return result;
}

static void sleep(int seconds)
{
	Sleep(seconds * 1000);
}

/*
 * There is no setenv on Windows, so it should be safe for us to
 * define this compatible version.
 */
static int setenv(const char *name, const char *value, int overwrite)
{
	struct string *string;

	if (!overwrite && getenv(name))
		return 0;
	if ((!name) || (!value))
		return 0;

	string = string_initf("%s=%s", name, value);
	return putenv(string->buffer);
}

/* Similarly we can do the same for unsetenv: */
static int unsetenv(const char *name)
{
	struct string *string = string_initf("%s=", name);
	return putenv(string->buffer);
}

static void win_verror(const char *fmt, va_list ap)
{
	struct string *string = string_init(32);

	string_vaddf(string, fmt, ap);
	MessageBox(NULL, string->buffer, "ImageJ Error", MB_OK);
	string_release(string);
}

static MAYBE_UNUSED void win_error(const char *fmt, ...)
{
	va_list ap;

	va_start(ap, fmt);
	win_verror(fmt, ap);
	va_end(ap);
}

#else
#include <dlfcn.h>
#endif

/* A wrapper for setenv that exits on error */
void setenv_or_exit(const char *name, const char *value, int overwrite)
{
	int result;
	if (!value) {
#ifdef __APPLE__
		unsetenv(name);
#else
		result = unsetenv(name);
		if (result)
			die("Unsetting environment variable %s failed", name);
#endif
		return;
	}
	result = setenv(name, value, overwrite);
	if (result)
		die("Setting environment variable %s to %s failed", name, value);
}

/* Determining heap size */

#ifdef __APPLE__
#include <mach/mach_init.h>
#include <mach/mach_host.h>

size_t get_memory_size(int available_only)
{
	host_priv_t host = mach_host_self();
	vm_size_t page_size;
	vm_statistics_data_t host_info;
	mach_msg_type_number_t host_count =
		sizeof(host_info) / sizeof(integer_t);

	host_page_size(host, &page_size);
	return host_statistics(host, HOST_VM_INFO,
			(host_info_t)&host_info, &host_count) ?
		0 : ((size_t)(available_only ? host_info.free_count :
				host_info.active_count +
				host_info.inactive_count +
				host_info.wire_count) * (size_t)page_size);
}
#elif defined(__linux__)
static size_t get_kB(struct string *string, const char *key)
{
	const char *p = strstr(string->buffer, key);
	if (!p)
		return 0;
	while (*p && *p != ' ')
		p++;
	return (size_t)strtoul(p, NULL, 10);
}

size_t get_memory_size(int available_only)
{
	ssize_t page_size, available_pages;
	/* Avoid overallocation */
	if (!available_only) {
		struct string *string = string_init(32);
		if (!string_read_file(string, "/proc/meminfo"))
			return 1024 * (get_kB(string, "MemFree:")
				+ get_kB(string, "Buffers:")
				+ get_kB(string, "Cached:"));
	}
	page_size = sysconf(_SC_PAGESIZE);
	available_pages = sysconf(available_only ?
		_SC_AVPHYS_PAGES : _SC_PHYS_PAGES);
	return page_size < 0 || available_pages < 0 ?
		0 : (size_t)page_size * (size_t)available_pages;
}
#elif defined(WIN32)
#include <windows.h>

size_t get_memory_size(int available_only)
{
	MEMORYSTATUS status;

	GlobalMemoryStatus(&status);
	return available_only ? status.dwAvailPhys : status.dwTotalPhys;
}
#else
size_t get_memory_size(int available_only)
{
	fprintf(stderr, "Cannot reserve optimal memory on this platform\n");
	return 0;
}
#endif

/* This returns the amount of megabytes */
static long parse_memory(const char *amount)
{
	char *endp;
	long result = strtol(amount, &endp, 0);

	if (endp)
		switch (*endp) {
		case 't': case 'T':
			result <<= 10;
			/* fall through */
		case 'g': case 'G':
			result <<= 10;
			/* fall through */
		case 'm': case 'M':
		case '\0':
			/* fall back to megabyte */
			break;
		default:
			die("Unsupported memory unit '%c' in %s", *endp, amount);
		}

	return result;
}

static MAYBE_UNUSED int parse_bool(const char *value)
{
	return strcmp(value, "0") && strcmp(value, "false") &&
		strcmp(value, "False") && strcmp(value, "FALSE");
}

/* work around a SuSE IPv6 setup bug */

#ifdef IPV6_MAYBE_BROKEN
#include <netinet/ip6.h>
#include <fcntl.h>
#endif

static int is_ipv6_broken(void)
{
#ifndef IPV6_MAYBE_BROKEN
	return 0;
#else
	int sock = socket(AF_INET6, SOCK_STREAM, 0);
	static const struct in6_addr in6addr_loopback = {
		{ { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1 } }
	};
	struct sockaddr_in6 address = {
		AF_INET6, 57294 + 7, 0, in6addr_loopback, 0
	};
	int result = 0;
	long flags;

	if (sock < 0)
		return 1;

	flags = fcntl(sock, F_GETFL, NULL);
	if (fcntl(sock, F_SETFL, flags | O_NONBLOCK) < 0) {
		close(sock);
		return 1;
	}


	if (connect(sock, (struct sockaddr *)&address, sizeof(address)) < 0) {
		if (errno == EINPROGRESS) {
			struct timeval tv;
			fd_set fdset;

			tv.tv_sec = 0;
			tv.tv_usec = 50000;
			FD_ZERO(&fdset);
			FD_SET(sock, &fdset);
			if (select(sock + 1, NULL, &fdset, NULL, &tv) > 0) {
				int error;
				socklen_t length = sizeof(int);
				if (getsockopt(sock, SOL_SOCKET, SO_ERROR,
						(void*)&error, &length) < 0)
					result = 1;
				else
					result = (error == EACCES) |
						(error == EPERM) |
						(error == EAFNOSUPPORT) |
						(error == EINPROGRESS);
			} else
				result = 1;
		} else
			result = (errno == EACCES) | (errno == EPERM) |
				(errno == EAFNOSUPPORT);
	}

	close(sock);
	return result;
#endif
}


/* Java stuff */

#ifndef JNI_CREATEVM
#define JNI_CREATEVM "JNI_CreateJavaVM"
#endif

static int is_slash(char c)
{
#ifdef WIN32
	if (c == '\\')
		return 1;
#endif
	return c == '/';
}

const char *ij_dir;

static const char *ij_path(const char *relative_path)
{
	static struct string *string[3];
	static int counter;

	counter = ((counter + 1) % (sizeof(string) / sizeof(string[0])));
	if (!string[counter])
		string[counter] = string_initf("%s%s%s", ij_dir,
			is_slash(*relative_path) ? "" : "/", relative_path);
	else
		string_setf(string[counter], "%s%s%s", ij_dir,
			is_slash(*relative_path) ? "" : "/", relative_path);
	return string[counter]->buffer;
}

char *main_argv0;
char **main_argv, **main_argv_backup;
int main_argc, main_argc_backup;
const char *main_class;
int run_precompiled = 0;

static int dir_exists(const char *directory);
static int is_native_library(const char *path);
static int file_exists(const char *path);

static MAYBE_UNUSED const char *get_java_home_env(void)
{
	const char *env = getenv("JAVA_HOME");
	if (env) {
		if (dir_exists(env)) {
			struct string* libjvm =
				string_initf("%s/%s", env, library_path);
			if (!file_exists(libjvm->buffer)) {
				string_set_length(libjvm, 0);
				string_addf(libjvm, "%s/jre/%s", env, library_path);
			}
			if (file_exists(libjvm->buffer) &&
					!is_native_library(libjvm->buffer)) {
				error("Ignoring JAVA_HOME (wrong arch): %s",
					env);
				env = NULL;
			}
			string_release(libjvm);
			if (env)
				return env;
		}
		else
			error("Ignoring invalid JAVA_HOME: %s", env);
		unsetenv("JAVA_HOME");
	}
	return NULL;
}

static const char *get_java_home(void)
{
	if (absolute_java_home)
		return absolute_java_home;
	if (!relative_java_home)
		return NULL;
	return ij_path(relative_java_home);
}

static const char *get_jre_home(void)
{
	const char *result = get_java_home();
	int len;
	static struct string *jre;

	if (jre)
		return jre->buffer;

	if (!result)
		return NULL;
	len = strlen(result);
	if (len > 4 && !strcmp(result + len - 4, "/jre"))
		return result;

	jre = string_initf("%s/jre", result);
	if (dir_exists(jre->buffer))
		return jre->buffer;
	string_setf(jre, "%s", result);
	return result;
}

static size_t mystrlcpy(char *dest, const char *src, size_t size)
{
	size_t ret = strlen(src);

	if (size) {
		size_t len = (ret >= size) ? size - 1 : ret;
		memcpy(dest, src, len);
		dest[len] = '\0';
	}
	return ret;
}

const char *last_slash(const char *path)
{
	const char *slash = strrchr(path, '/');
#ifdef WIN32
	const char *backslash = strrchr(path, '\\');

	if (backslash && slash < backslash)
		slash = backslash;
#endif
	return slash;
}

#ifndef PATH_MAX
#define PATH_MAX 1024
#endif

static const char *make_absolute_path(const char *path)
{
	static char bufs[2][PATH_MAX + 1], *buf = bufs[0];
	char cwd[PATH_MAX] = "";
#ifndef WIN32
	static char *next_buf = bufs[1];
	int buf_index = 1, len;
#endif

	int depth = 20;
	char *last_elem = NULL;
	struct stat st;

	if (mystrlcpy(buf, path, PATH_MAX) >= PATH_MAX)
		die("Too long path: %s", path);

	while (depth--) {
		if (stat(buf, &st) || !S_ISDIR(st.st_mode)) {
			const char *slash = last_slash(buf);
			if (slash) {
				buf[slash-buf] = '\0';
				last_elem = xstrdup(slash + 1);
			} else {
				last_elem = xstrdup(buf);
				*buf = '\0';
			}
		}

		if (*buf) {
			if (!*cwd && !getcwd(cwd, sizeof(cwd)))
				die("Could not get current working dir");

			if (chdir(buf))
				die("Could not switch to %s", buf);
		}
		if (!getcwd(buf, PATH_MAX))
			die("Could not get current working directory");

		if (last_elem) {
			int len = strlen(buf);
			if (len + strlen(last_elem) + 2 > PATH_MAX)
				die("Too long path name: %s/%s", buf, last_elem);
			buf[len] = '/';
			strcpy(buf + len + 1, last_elem);
			free(last_elem);
			last_elem = NULL;
		}

#ifndef WIN32
		if (!lstat(buf, &st) && S_ISLNK(st.st_mode)) {
			len = readlink(buf, next_buf, PATH_MAX);
			if (len < 0)
				die("Invalid symlink: %s", buf);
			next_buf[len] = '\0';
			buf = next_buf;
			buf_index = 1 - buf_index;
			next_buf = bufs[buf_index];
		} else
#endif
			break;
	}

	if (*cwd && chdir(cwd))
		die("Could not change back to %s", cwd);

	return buf;
}

static int is_absolute_path(const char *path)
{
#ifdef WIN32
	if (((path[0] >= 'A' && path[0] <= 'Z') ||
			(path[0] >= 'a' && path[0] <= 'z')) && path[1] == ':')
		return 1;
#endif
	return path[0] == '/';
}

static int file_exists(const char *path)
{
	return !access(path, R_OK);
}

static inline int prefixcmp(const char *string, const char *prefix)
{
	return strncmp(string, prefix, strlen(prefix));
}

static inline int suffixcmp(const char *string, int len, const char *suffix)
{
	int suffix_len = strlen(suffix);
	if (len < suffix_len)
		return -1;
	return strncmp(string + len - suffix_len, suffix, suffix_len);
}

static const char *find_in_path(const char *path)
{
	const char *p = getenv("PATH");
	struct string *buffer;

#ifdef WIN32
	int len = strlen(path);
	struct string *path_with_suffix = NULL;
	const char *in_cwd;

	if (suffixcmp(path, len, ".exe") && suffixcmp(path, len, ".EXE")) {
		path_with_suffix = string_initf("%s.exe", path);
		path = path_with_suffix->buffer;
	}
	in_cwd = make_absolute_path(path);
	if (file_exists(in_cwd)) {
		string_release(path_with_suffix);
		return in_cwd;
	}
#endif

	if (!p)
		die("Could not get PATH");

	buffer = string_init(32);
	for (;;) {
		const char *colon = strchr(p, PATH_SEP[0]), *orig_p = p;
		int len = colon ? colon - p : strlen(p);
		struct stat st;

		if (!len)
			die("Could not find %s in PATH", path);

		p += len + !!colon;
		if (!is_absolute_path(orig_p))
			continue;
		string_setf(buffer, "%.*s/%s", len, orig_p, path);
#ifdef WIN32
#define S_IX S_IXUSR
#else
#define S_IX (S_IXUSR | S_IXGRP | S_IXOTH)
#endif
		if (!stat(buffer->buffer, &st) && S_ISREG(st.st_mode) &&
				(st.st_mode & S_IX)) {
			const char *result = make_absolute_path(buffer->buffer);
			string_release(buffer);
#ifdef WIN32
			string_release(path_with_suffix);
#endif
			return result;
		}
	}
}

#ifdef WIN32
static char *dos_path(const char *path)
{
	const char *orig = path;
	int size = GetShortPathName(path, NULL, 0);
	char *buffer;

	if (!size)
		path = find_in_path(path);
	size = GetShortPathName(path, NULL, 0);
	if (!size)
		die ("Could not determine DOS name of %s", orig);
	buffer = (char *)xmalloc(size);
	GetShortPathName(path, buffer, size);
	return buffer;
}
#endif

static MAYBE_UNUSED struct string *get_parent_directory(const char *path)
{
	const char *slash = last_slash(path);

	if (!slash || slash == path)
		return string_initf("/");
	return string_initf("%.*s", (int)(slash - path), path);
}

static int find_file(struct string *search_root, int max_depth, const char *file, struct string *result);

/* Splash screen */

static int no_splash;
static void (*SplashClose)(void);

struct string *get_splashscreen_lib_path(void)
{
	const char *jre_home = get_jre_home();
#if defined(WIN32)
	return !jre_home ? NULL : string_initf("%s/bin/splashscreen.dll", jre_home);
#elif defined(__linux__)
	return !jre_home ? NULL : string_initf("%s/lib/%s/libsplashscreen.so", jre_home, sizeof(void *) == 8 ? "amd64" : "i386");
#elif defined(__APPLE__)
	struct string *search_root = string_initf("/System/Library/Java/JavaVirtualMachines");
	struct string *result = string_init(32);
	if (!find_file(search_root, 4, "libsplashscreen.jnilib", result)) {
		string_release(result);
		result = NULL;
	}
	string_release(search_root);
	return result;
#else
	return NULL;
#endif
}

/* So far, only Windows and MacOSX support splash with alpha, Linux does not */
#if defined(WIN32) || defined(__APPLE__)
#define SPLASH_PATH "images/icon.png"
#else
#define SPLASH_PATH "images/icon-flat.png"
#endif

static void show_splash(void)
{
	const char *image_path = ij_path(SPLASH_PATH);
	struct string *lib_path = get_splashscreen_lib_path();
	void *splashscreen;
	int (*SplashInit)(void);
	int (*SplashLoadFile)(const char *path);
	int (*SplashSetFileJarName)(const char *file_path, const char *jar_path);

	if (no_splash || !lib_path || SplashClose)
		return;
	splashscreen = dlopen(lib_path->buffer, RTLD_LAZY);
	if (!splashscreen) {
		string_release(lib_path);
		return;
	}
	SplashInit = dlsym(splashscreen, "SplashInit");
	SplashLoadFile = dlsym(splashscreen, "SplashLoadFile");
	SplashSetFileJarName = dlsym(splashscreen, "SplashSetFileJarName");
	SplashClose = dlsym(splashscreen, "SplashClose");
	if (!SplashInit || !SplashLoadFile || !SplashSetFileJarName || !SplashClose) {
		string_release(lib_path);
		SplashClose = NULL;
		return;
	}

	SplashInit();
	SplashLoadFile(image_path);
	SplashSetFileJarName(image_path, ij_path("jars/ij-launcher.jar"));

	string_release(lib_path);
}

static void hide_splash(void)
{
	if (!SplashClose)
		return;
	SplashClose();
	SplashClose = NULL;
}

/*
 * On Linux, JDK5 does not find the library path with libmlib_image.so,
 * so we have to add that explicitely to the LD_LIBRARY_PATH.
 *
 * Unfortunately, ld.so only looks at LD_LIBRARY_PATH at startup, so we
 * have to reexec after setting that variable.
 *
 * See also line 140ff of
 * http://hg.openjdk.java.net/jdk6/jdk6/hotspot/file/14f7b2425c86/src/os/solaris/launcher/java_md.c
 */
static void maybe_reexec_with_correct_lib_path(void)
{
#ifdef __linux__
	const char *jre_home = get_jre_home(), *original;
	struct string *path, *parent, *lib_path, *jli;

	if (!jre_home)
		return;

	path = string_initf("%s/%s", jre_home, library_path);
	parent = get_parent_directory(path->buffer);
	lib_path = get_parent_directory(parent->buffer);
	jli = string_initf("%s/jli", lib_path->buffer);

	string_release(path);
	string_release(parent);

	/* Is this JDK6? */
	if (!dir_exists(get_jre_home()) || dir_exists(jli->buffer)) {
		string_release(lib_path);
		string_release(jli);
		return;
	}
	string_release(jli);

	original = getenv("LD_LIBRARY_PATH");
	if (original && path_list_contains(original, lib_path->buffer)) {
		string_release(lib_path);
		return;
	}

	if (original)
		string_append_path_list(lib_path, original);
	setenv_or_exit("LD_LIBRARY_PATH", lib_path->buffer, 1);
	error("Re-executing with correct library lookup path");
	hide_splash();
	execv(main_argv_backup[0], main_argv_backup);
	die("Could not re-exec with correct library lookup!");
#endif
}

static const char *get_ij_dir(const char *argv0)
{
	static const char *buffer;
	const char *slash;
	int len;

	if (buffer)
		return buffer;

	if (!last_slash(argv0))
		buffer = find_in_path(argv0);
	else
		buffer = make_absolute_path(argv0);
	argv0 = buffer;

	slash = last_slash(argv0);
	if (!slash)
		die("Could not get absolute path for executable");

	len = slash - argv0;
	if (!suffixcmp(argv0, len, "/precompiled") ||
			!suffixcmp(argv0, len, "\\precompiled")) {
		slash -= strlen("/precompiled");
		run_precompiled = 1;
	}
#ifdef __APPLE__
	else if (!suffixcmp(argv0, len, "/Contents/MacOS")) {
		struct string *scratch;
		len -= strlen("/Contents/MacOS");
		scratch = string_initf("%.*s/jars/ij-launcher.jar", len, argv0);
		if (len && !file_exists(scratch->buffer))
			while (--len && argv0[len] != '/')
				; /* ignore */
		slash = argv0 + len;
	}
#endif
#ifdef WIN32
	else if (!suffixcmp(argv0, len, "/PRECOM~1") ||
			!suffixcmp(argv0, len, "\\PRECOM~1")) {
		slash -= strlen("/PRECOM~1");
		run_precompiled = 1;
	}
#endif

	buffer = xstrndup(buffer, slash - argv0);
#ifdef WIN32
	buffer = dos_path(buffer);
#endif
	return buffer;
}

static int create_java_vm(JavaVM **vm, void **env, JavaVMInitArgs *args)
{
#ifdef __APPLE__
	set_path_to_JVM();
#else
	/*
	 * Save the original value of JAVA_HOME: if creating the JVM this
	 * way doesn't work, set it back so that calling the system JVM
	 * can use the JAVA_HOME variable if it's set...
	 */
	char *original_java_home_env = getenv("JAVA_HOME");
	struct string *buffer = string_init(32);
	void *handle;
	char *err;
	static jint (*JNI_CreateJavaVM)(JavaVM **pvm, void **penv, void *args);
	const char *java_home = get_jre_home();

#ifdef WIN32
	/* On Windows, a setenv() invalidates strings obtained by getenv(). */
	if (original_java_home_env)
		original_java_home_env = xstrdup(original_java_home_env);
#endif

	setenv_or_exit("JAVA_HOME", java_home, 1);

	string_addf(buffer, "%s/%s", java_home, library_path);

	handle = dlopen(buffer->buffer, RTLD_LAZY);
	if (!handle) {
		const char *err;
		setenv_or_exit("JAVA_HOME", original_java_home_env, 1);
		if (!file_exists(java_home))
			return 2;

		err = dlerror();
		if (!err)
			err = "(unknown error)";
		error("Could not load Java library '%s': %s",
			buffer->buffer, err);
		return 1;
	}
	dlerror(); /* Clear any existing error */

	JNI_CreateJavaVM = dlsym(handle, JNI_CREATEVM);
	err = dlerror();
	if (err) {
		error("Error loading libjvm: %s", err);
		setenv_or_exit("JAVA_HOME", original_java_home_env, 1);
		return 1;
	}
#endif

	return JNI_CreateJavaVM(vm, env, args);
}

/* Windows specific stuff */

#ifdef WIN32
static int console_opened, console_attached;

static void sleep_a_while(void)
{
	sleep(60);
}

static void open_win_console(void)
{
	static int initialized = 0;
	struct string *kernel32_dll_path;
	void *kernel32_dll;
	BOOL WINAPI (*attach_console)(DWORD process_id) = NULL;
	SECURITY_ATTRIBUTES attributes;
	HANDLE handle;

	if (initialized)
		return;
	initialized = 1;
	console_attached = 1;
	if (!isatty(1) && !isatty(2))
		return;

	kernel32_dll_path = string_initf("%s\\system32\\kernel32.dll",
		getenv("WINDIR"));
	kernel32_dll = dlopen(kernel32_dll_path->buffer, RTLD_LAZY);
	string_release(kernel32_dll_path);
	if (kernel32_dll)
		attach_console = (typeof(attach_console))
			dlsym(kernel32_dll, "AttachConsole");
	if (!attach_console || !attach_console((DWORD)-1)) {
		if (attach_console) {
			if (GetLastError() == ERROR_ACCESS_DENIED)
				/*
				 * Already attached, according to
				 * http://msdn.microsoft.com/en-us/library/windows/desktop/ms681952(v=vs.85).aspx
				 */
				return;
			error("error attaching console: %s", get_win_error());
		}
		AllocConsole();
		console_opened = 1;
		atexit(sleep_a_while);
	} else {
		char title[1024];
		if (GetConsoleTitle(title, sizeof(title)) &&
				!strncmp(title, "rxvt", 4))
			return; /* Console already opened. */
	}

	memset(&attributes, 0, sizeof(attributes));
	attributes.nLength = sizeof(SECURITY_ATTRIBUTES);
	attributes.bInheritHandle = TRUE;

	handle = CreateFile("CONOUT$", GENERIC_WRITE, FILE_SHARE_WRITE,
		&attributes, OPEN_EXISTING, 0, NULL);
	if (isatty(1)) {
		freopen("CONOUT$", "wt", stdout);
		SetStdHandle(STD_OUTPUT_HANDLE, handle);
	}
	if (isatty(2)) {
		freopen("CONOUT$", "wb", stderr);
		SetStdHandle(STD_ERROR_HANDLE, handle);
	}
}


static int fake_posix_mkdir(const char *name, int mode)
{
	return mkdir(name);
}
#define mkdir fake_posix_mkdir


struct entry {
	char d_name[PATH_MAX];
	int d_namlen;
} entry;

struct dir {
	struct string *pattern;
	HANDLE handle;
	WIN32_FIND_DATA find_data;
	int done;
	struct entry entry;
};

struct dir *open_dir(const char *path)
{
	struct dir *result = xcalloc(sizeof(struct dir), 1);
	if (!result)
		return result;
	result->pattern = string_initf("%s/*", path);
	result->handle = FindFirstFile(result->pattern->buffer,
			&(result->find_data));
	if (result->handle == INVALID_HANDLE_VALUE) {
		string_release(result->pattern);
		free(result);
		return NULL;
	}
	result->done = 0;
	return result;
}

struct entry *read_dir(struct dir *dir)
{
	if (dir->done)
		return NULL;
	strcpy(dir->entry.d_name, dir->find_data.cFileName);
	dir->entry.d_namlen = strlen(dir->entry.d_name);
	if (FindNextFile(dir->handle, &dir->find_data) == 0)
		dir->done = 1;
	return &dir->entry;
}

int close_dir(struct dir *dir)
{
	string_release(dir->pattern);
	FindClose(dir->handle);
	free(dir);
	return 0;
}

#define DIR struct dir
#define dirent entry
#define opendir open_dir
#define readdir read_dir
#define closedir close_dir
#else
#include <dirent.h>
#endif

static int dir_exists(const char *path)
{
	DIR *dir = opendir(path);
	if (dir) {
		closedir(dir);
		return 1;
	}
	return 0;
}

static int mkdir_recursively(struct string *buffer)
{
	int slash = buffer->length - 1, save_length;
	char save_char;
	while (slash > 0 && !is_slash(buffer->buffer[slash]))
		slash--;
	while (slash > 0 && is_slash(buffer->buffer[slash - 1]))
		slash--;
	if (slash <= 0)
		return -1;
	save_char = buffer->buffer[slash];
	save_length = buffer->length;
	buffer->buffer[slash] = '\0';
	buffer->length = slash;
	if (!dir_exists(buffer->buffer)) {
		int result = mkdir_recursively(buffer);
		if (result)
			return result;
	}
	buffer->buffer[slash] = save_char;
	buffer->length = save_length;
	return mkdir(buffer->buffer, 0777);
}

/*
   Ensures that a directory exists in the manner of "mkdir -p", creating
   components with file mode 777 (& umask) where they do not exist.
   Returns 0 on success, or the return code of mkdir in the case of
   failure.
*/
static int mkdir_p(const char *path)
{
	int result;
	struct string *buffer;
	if (dir_exists(path))
		return 0;

	buffer = string_copy(path);
	result = mkdir_recursively(buffer);
	string_release(buffer);
	return result;
}

static MAYBE_UNUSED int find_file(struct string *search_root, int max_depth, const char *file, struct string *result)
{
	int len = search_root->length;
	DIR *directory;
	struct dirent *entry;

	string_add_char(search_root, '/');

	string_append(search_root, file);
	if (file_exists(search_root->buffer)) {
		string_set(result, search_root->buffer);
		string_set_length(search_root, len);
		return 1;
	}

	if (max_depth <= 0)
		return 0;

	string_set_length(search_root, len);
	directory = opendir(search_root->buffer);
	if (!directory)
		return 0;
	string_add_char(search_root, '/');
	while (NULL != (entry = readdir(directory))) {
		if (entry->d_name[0] == '.')
			continue;
		string_append(search_root, entry->d_name);
		if (dir_exists(search_root->buffer))
			if (find_file(search_root, max_depth - 1, file, result)) {
				string_set_length(search_root, len);
				closedir(directory);
				return 1;
			}
		string_set_length(search_root, len + 1);
	}
	closedir(directory);
	string_set_length(search_root, len);
	return 0;
}

static void detect_library_path(struct string *library_path, struct string *directory)
{
	int original_length = directory->length;
	char found = 0;
	DIR *dir = opendir(directory->buffer);
	struct dirent *entry;

	if (!dir)
		return;

	while ((entry = readdir(dir))) {
		if (entry->d_name[0] == '.')
			continue;
		string_addf(directory, "/%s", entry->d_name);
		if (dir_exists(directory->buffer))
			detect_library_path(library_path, directory);
		else if (!found && is_native_library(directory->buffer)) {
			string_set_length(directory, original_length);
			string_append_path_list(library_path, directory->buffer);
			found = 1;
			continue;
		}
		string_set_length(directory, original_length);
	}
	closedir(dir);
}

static void add_java_home_to_path(void)
{
	const char *java_home = get_java_home();
	struct string *new_path = string_init(32), *buffer;
	const char *env;

	if (!java_home)
		return;
	buffer = string_initf("%s/bin", java_home);
	if (dir_exists(buffer->buffer))
		string_append_path_list(new_path, buffer->buffer);
	string_setf(buffer, "%s/jre/bin", java_home);
	if (dir_exists(buffer->buffer))
		string_append_path_list(new_path, buffer->buffer);

	env = getenv("PATH");
	string_append_path_list(new_path, env ? env : ij_dir);
	setenv_or_exit("PATH", new_path->buffer, 1);
	string_release(buffer);
	string_release(new_path);
}

static int headless, headless_argc;

static struct string *set_property(JNIEnv *env,
		const char *key, const char *value)
{
	static jclass system_class = NULL;
	static jmethodID set_property_method = NULL;
	jstring result;

	if (!value)
		return NULL;

	if (!system_class) {
		system_class = (*env)->FindClass(env, "java/lang/System");
		if (!system_class)
			return NULL;
	}

	if (!set_property_method) {
		set_property_method = (*env)->GetStaticMethodID(env, system_class,
				"setProperty",
				"(Ljava/lang/String;Ljava/lang/String;)"
				"Ljava/lang/String;");
		if (!set_property_method)
			return NULL;
	}

	result = (jstring)(*env)->CallStaticObjectMethod(env, system_class,
				set_property_method,
				(*env)->NewStringUTF(env, key),
				(*env)->NewStringUTF(env, value));
	if (result) {
		const char *chars = (*env)->GetStringUTFChars(env, result, NULL);
		struct string *res = string_copy(chars);
		(*env)->ReleaseStringUTFChars(env, result, chars);
		return res;
	}

	return NULL;
}

struct string_array {
	char **list;
	int nr, alloc;
};

static void append_string(struct string_array *array, char *str)
{
	if (array->nr >= array->alloc) {
		array->alloc = 2 * array->nr + 16;
		array->list = (char **)xrealloc(array->list,
				array->alloc * sizeof(str));
	}
	array->list[array->nr++] = str;
}

static void prepend_string(struct string_array *array, char *str)
{
	if (array->nr >= array->alloc) {
		array->alloc = 2 * array->nr + 16;
		array->list = (char **)xrealloc(array->list,
				array->alloc * sizeof(str));
	}
	memmove(array->list + 1, array->list, array->nr * sizeof(str));
	array->list[0] = str;
	array->nr++;
}

static void prepend_string_copy(struct string_array *array, const char *str)
{
	prepend_string(array, xstrdup(str));
}

static void append_string_array(struct string_array *target,
		struct string_array *source)
{
	if (target->alloc - target->nr < source->nr) {
		target->alloc += source->nr;
		target->list = (char **)xrealloc(target->list,
				target->alloc * sizeof(target->list[0]));
	}
	memcpy(target->list + target->nr, source->list, source->nr * sizeof(target->list[0]));
	target->nr += source->nr;
}

static void prepend_string_array(struct string_array *target,
		struct string_array *source)
{
	if (source->nr <= 0)
		return;
	if (target->alloc - target->nr < source->nr) {
		target->alloc += source->nr;
		target->list = (char **)xrealloc(target->list,
				target->alloc * sizeof(target->list[0]));
	}
	memmove(target->list + source->nr, target->list, target->nr * sizeof(target->list[0]));
	memcpy(target->list, source->list, source->nr * sizeof(target->list[0]));
	target->nr += source->nr;
}

static JavaVMOption *prepare_java_options(struct string_array *array)
{
	JavaVMOption *result = (JavaVMOption *)xcalloc(array->nr,
			sizeof(JavaVMOption));
	int i;

	for (i = 0; i < array->nr; i++)
		result[i].optionString = array->list[i];

	return result;
}

static jobjectArray prepare_ij_options(JNIEnv *env, struct string_array* array)
{
	jstring jstr;
	jobjectArray result;
	int i;

	if (!(jstr = (*env)->NewStringUTF(env, array->nr ? array->list[0] : ""))) {
fail:
		(*env)->ExceptionDescribe(env);
		die("Failed to create ImageJ option array");
	}

	result = (*env)->NewObjectArray(env, array->nr,
			(*env)->FindClass(env, "java/lang/String"), jstr);
	if (!result)
		goto fail;
	for (i = 1; i < array->nr; i++) {
		if (!(jstr = (*env)->NewStringUTF(env, array->list[i])))
			goto fail;
		(*env)->SetObjectArrayElement(env, result, i, jstr);
	}
	return result;
}

struct options {
	struct string_array java_options, launcher_options, ij_options;
	int debug, use_system_jvm;
};

static void add_launcher_option(struct options *options, const char *option, const char *class_path)
{
	append_string(&options->launcher_options, xstrdup(option));
	if (class_path)
		append_string(&options->launcher_options, xstrdup(class_path));
}

static void add_tools_jar(struct options *options)
{
	const char *jre_home = get_jre_home();
	struct string *string;

	if (!jre_home)
		die("Cannot determine path to tools.jar");

	string = string_initf("%s/../lib/tools.jar", jre_home);
	add_launcher_option(options, "-classpath", string->buffer);
	string_release(string);
}

static void add_option(struct options *options, char *option, int for_ij)
{
	append_string(for_ij ?
			&options->ij_options : &options->java_options, option);
}

static void add_option_copy(struct options *options, const char *option, int for_ij)
{
	add_option(options, xstrdup(option), for_ij);
}

static void add_option_string(struct options *options, struct string *option, int for_ij)
{
	add_option(options, xstrdup(option->buffer), for_ij);
}

static int is_quote(char c)
{
	return c == '\'' || c == '"';
}

static int find_closing_quote(const char *s, char quote, int index, int len)
{
	int i;

	for (i = index; i < len; i++) {
		char c = s[i];
		if (c == quote)
			return i;
		if (is_quote(c))
			i = find_closing_quote(s, c, i + 1, len);
	}
	fprintf(stderr, "Unclosed quote: %s\n               ", s);
	for (i = 0; i < index; i++)
		fputc(' ', stderr);
	die("^");
}

static void add_options(struct options *options, const char *cmd_line, int for_ij)
{
	int len = strlen(cmd_line), i;
	struct string *current = string_init(32);

	for (i = 0; i < len; i++) {
		char c = cmd_line[i];
		if (is_quote(c)) {
			int i2 = find_closing_quote(cmd_line, c, i + 1, len);
			string_append_at_most(current, cmd_line + i + 1, i2 - i - 1);
			i = i2;
			continue;
		}
		if (c == ' ' || c == '\t' || c == '\n') {
			if (!current->length)
				continue;
			add_option_string(options, current, for_ij);
			string_set_length(current, 0);
		} else
			string_add_char(current, c);
	}
	if (current->length)
		add_option_string(options, current, for_ij);

	string_release(current);
}

/*
 * If passing -Xmx=99999999g -Xmx=37m to Java, the former still triggers an
 * error. So let's keep only the last, so that the command line can override
 * invalid settings in jvm.cfg.
 */
static void keep_only_one_memory_option(struct string_array *options)
{
	int index_Xmx = -1, index_Xms = -1, index_Xmn = -1;
	int i, j;

	for (i = options->nr - 1; i >= 0; i--)
		if (index_Xmx < 0 && !prefixcmp(options->list[i], "-Xmx"))
			index_Xmx = i;
		else if (index_Xms < 0 && !prefixcmp(options->list[i], "-Xms"))
			index_Xms = i;
		else if (index_Xmn < 0 && !prefixcmp(options->list[i], "-Xmn"))
			index_Xmn = i;

	for (i = j = 0; i < options->nr; i++)
		if ((i < index_Xmx && !prefixcmp(options->list[i], "-Xmx")) ||
				(i < index_Xms && !prefixcmp(options->list[i], "-Xms")) ||
				(i < index_Xmn && !prefixcmp(options->list[i], "-Xmn")))
			continue;
		else {
			if (i > j)
				options->list[j] = options->list[i];
			j++;
		}
	options->nr = j;
}

static const char* has_memory_option(struct string_array *options)
{
	int i;
	for (i = 0; i < options->nr; i++)
		if (!prefixcmp(options->list[i], "-Xm"))
			return options->list[i];
	return NULL;
}

static MAYBE_UNUSED void read_file_as_string(const char *file_name, struct string *contents)
{
	char buffer[1024];
	FILE *in = fopen(file_name, "r");

	string_set_length(contents, 0);
	if (!in)
		return;

	while (!feof(in)) {
		int count = fread(buffer, 1, sizeof(buffer), in);
		string_append_at_most(contents, buffer, count);
	}
	fclose(in);
}

static struct string *quote_if_necessary(const char *option)
{
	struct string *result = string_init(32);
	for (; *option; option++)
		switch (*option) {
		case '\n':
			string_append(result, "\\n");
			break;
		case '\t':
			string_append(result, "\\t");
			break;
		case ' ': case '"': case '\\':
			string_add_char(result, '\\');
			/* fallthru */
		default:
			string_add_char(result, *option);
			break;
		}
	return result;
}

#ifdef WIN32
/* fantastic win32 quoting */
static char *quote_win32(char *option)
{
	char *p, *result, *r1;
	int backslashes = 0;

	for (p = option; *p; p++)
		if (strchr(" \"\t", *p))
			backslashes++;

	if (!backslashes)
		return option;

	result = (char *)xmalloc(strlen(option) + backslashes + 2 + 1);
	r1 = result;
	*(r1++) = '"';
	for (p = option; *p; p++) {
		if (*p == '"')
			*(r1++) = '\\';
		*(r1++) = *p;
	}
	*(r1++) = '"';
	*(r1++) = '\0';

	return result;
}
#endif

static const char *get_java_command(void)
{
#ifdef WIN32
	if (!console_opened)
		return "javaw";
#endif
	return "java";
}

static void show_commandline(struct options *options)
{
	int j;

	printf("%s", get_java_command());
	for (j = 0; j < options->java_options.nr; j++) {
		struct string *quoted = quote_if_necessary(options->java_options.list[j]);
		printf(" %s", quoted->buffer);
		string_release(quoted);
	}
	printf(" %s", main_class);
	for (j = 0; j < options->ij_options.nr; j++) {
		struct string *quoted = quote_if_necessary(options->ij_options.list[j]);
		printf(" %s", quoted->buffer);
		string_release(quoted);
	}
	fputc('\n', stdout);
}

int file_is_newer(const char *path, const char *than)
{
	struct stat st1, st2;

	if (stat(path, &st1))
		return 0;
	return stat(than, &st2) || st1.st_mtime > st2.st_mtime;
}

int handle_one_option(int *i, const char **argv, const char *option, struct string *arg)
{
	int len;
	string_set_length(arg, 0);
	if (!strcmp(argv[*i], option)) {
		if (++(*i) >= main_argc || !argv[*i])
			die("Option %s needs an argument!", option);
		string_append(arg, argv[*i]);
		return 1;
	}
	len = strlen(option);
	if (!strncmp(argv[*i], option, len) && argv[*i][len] == '=') {
		string_append(arg, argv[*i] + len + 1);
		return 1;
	}
	return 0;
}

static int is_file_empty(const char *path)
{
	struct stat st;

	return !stat(path, &st) && !st.st_size;
}

static int update_files(struct string *relative_path)
{
	int len = relative_path->length, source_len, target_len;
	struct string *source = string_initf("%s/update%s",
		ij_dir, relative_path->buffer), *target;
	DIR *directory = opendir(source->buffer);
	struct dirent *entry;

	if (!directory) {
		string_release(source);
		return 0;
	}
	target = string_copy(ij_path(relative_path->buffer));
	if (mkdir_p(target->buffer)) {
		string_release(source);
		string_release(target);
		die("Could not create directory: %s", relative_path->buffer);
	}
	string_add_char(source, '/');
	source_len = source->length;
	string_add_char(target, '/');
	target_len = target->length;
	while (NULL != (entry = readdir(directory))) {
		const char *filename = entry->d_name;

		if (!strcmp(filename, ".") || !strcmp(filename, ".."))
			continue;

		string_set_length(relative_path, len);
		string_addf(relative_path, "/%s", filename);
		if (update_files(relative_path)) {
			continue;
		}

		string_set_length(source, source_len);
		string_append(source, filename);
		string_set_length(target, target_len);
		string_append(target, filename);

		if (is_file_empty(source->buffer)) {
			if (unlink(source->buffer))
				error("Could not remove %s", source->buffer);
			if (unlink(target->buffer))
				error("Could not remove %s", target->buffer);
			continue;
		}

#ifdef WIN32
		if (file_exists(target->buffer) && unlink(target->buffer)) {
			if (!strcmp(filename, "ImageJ.exe") || !strcmp(filename, "ImageJ-win32.exe") || !strcmp(filename, "ImageJ-win64.exe")) {
				struct string *old = string_initf("%.*s.old.exe", target->length - 4, target->buffer);
				if (file_exists(old->buffer) && unlink(old->buffer))
					die("Could not move %s out of the way!", old->buffer);
				if (rename(target->buffer, old->buffer))
					die("Could not remove old version of %s.  Please move %s to %s manually!", target->buffer, source->buffer, target->buffer);
				string_release(old);
			}
			else
				die("Could not remove old version of %s.  Please remove it manually!", target->buffer);
		}
#endif
		if (rename(source->buffer, target->buffer))
			die("Could not move %s to %s: %s", source->buffer,
				target->buffer, strerror(errno));
	}
	closedir(directory);
	string_set_length(source, source_len - 1);
	rmdir(source->buffer);

	string_release(source);
	string_release(target);
	string_set_length(relative_path, len);

	return 1;
}

static void update_all_files(void)
{
	struct string *buffer = string_init(32);
	update_files(buffer);
	string_release(buffer);
}

/*
 * Flexible subcommand handling
 *
 * Every command line option of the form --<name> will be expanded to
 * <expanded>.
 */
struct subcommand
{
	char *name, *expanded;
	struct string description;
	struct {
		char **list;
		int alloc, size;
	} extensions;
};

struct {
	int alloc, size;
	struct subcommand *list;
} all_subcommands;

static int iswhitespace(char c)
{
	return c == ' ' || c == '\t' || c == '\n';
}

static void add_extension(struct subcommand *subcommand, const char *extension)
{
	int length = strlen(extension);

	while (length && iswhitespace(extension[length - 1]))
		length--;
	if (!length)
		return;

	if (subcommand->extensions.size + 1 >= subcommand->extensions.alloc) {
		int alloc = (16 + subcommand->extensions.alloc) * 3 / 2;
		subcommand->extensions.list = xrealloc(subcommand->extensions.list, alloc * sizeof(char *));
		subcommand->extensions.alloc = alloc;
	}
	subcommand->extensions.list[subcommand->extensions.size++] = xstrndup(extension, length);
}

/*
 * The files for subcommand configuration are of the form
 *
 * <option>: <command-line options to replacing the subcommand options>
 *  <description>
 *  [<possible continuation>]
 * [.extension]
 *
 * Example:
 *
 * --build --ij-jar=jars/fake.jar --main-class=fiji.build.Fake
 *  Start the Fiji Build in the current directory
 */
static void add_subcommand(const char *line)
{
	int size = all_subcommands.size;
	/* TODO: safeguard against malformed configuration files. */
	struct subcommand *latest = &all_subcommands.list[size - 1];

	/* Is it the description? */
	if (line[0] == ' ') {
		struct string *description = &latest->description;

		string_append(description, "\t");
		string_append(description, line + 1);
		string_append(description, "\n");
	}
	else if (line[0] == '-') {
		struct subcommand *current;
		const char *space;
		int length = strlen(line);

		if (length && line[length - 1] == '\n')
			length --;
		if (length && line[length - 1] == '\r')
			length --;
		if (!length)
			return;

		if (size == all_subcommands.alloc) {
			int alloc = (size + 16) * 3 / 2;
			all_subcommands.list = xrealloc(all_subcommands.list,
				alloc * sizeof(struct subcommand));
			all_subcommands.alloc = alloc;
		}

		current = &all_subcommands.list[size];
		memset(current, 0, sizeof(struct subcommand));
		space = strchr(line, ' ');
		if (space) {
			current->name = xstrndup(line, space - line);
			current->expanded = xstrndup(space + 1,
				length - (space + 1 - line));
		}
		else
			current->name = xstrndup(line, length);
		all_subcommands.size++;
	}
	else if (line[0] == '.') {
		add_extension(latest, line);
	}
}

const char *default_subcommands[] = {
	"--update --ij-jar=plugins/Fiji_Updater.jar --ij-jar=jars/jsch.jar --main-class=fiji.updater.Main",
	" start the command-line version of the ImageJ updater",
	"--jython --ij-jar=jars/jython.jar --full-classpath --main-class=org.python.util.jython",
	".py",
	" start Jython instead of ImageJ (this is the",
	" default when called with a file ending in .py)",
	"--jruby --ij-jar=jars/jruby.jar --full-classpath --main-class=org.jruby.Main",
	".rb",
	" start JRuby instead of ImageJ (this is the",
	" default when called with a file ending in .rb)",
	"--clojure --ij-jar=jars/clojure.jar --full-classpath --main-class=clojure.lang.Repl",
	".clj",
	" start Clojure instead of ImageJ (this is the """,
	" default when called with a file ending in .clj)",
	"--beanshell --ij-jar=jars/bsh.jar --full-classpath --main-class=bsh.Interpreter",
	".bs",
	"--bsh --ij-jar=jars/bsh.jar --full-classpath --main-class=bsh.Interpreter",
	".bsh",
	" start BeanShell instead of ImageJ (this is the",
	" default when called with a file ending in .bs or .bsh",
	"--javascript --ij-jar=jars/js.jar --full-classpath --main-class=org.mozilla.javascript.tools.shell.Main",
	"--js --ij-jar=jars/js.jar --full-classpath --main-class=org.mozilla.javascript.tools.shell.Main",
	".js",
	" start Javascript (the Rhino engine) instead of",
	" ImageJ (this is the default when called with a",
	" file ending in .js)",
	"--ant --tools-jar --ij-jar=jars/ant.jar --ij-jar=jars/ant-launcher.jar --ij-jar=jars/ant-nodeps.jar --ij-jar=jars/ant-junit.jar --dont-patch-ij1 --headless --main-class=org.apache.tools.ant.Main",
	" run Apache Ant",
	"--mini-maven --ij-jar=jars/fake.jar --dont-patch-ij1 --main-class=fiji.build.MiniMaven",
	" run Fiji's very simple Maven mockup",
	"--javac --ij-jar=jars/javac.jar --freeze-classloader --headless --full-classpath --dont-patch-ij1 --pass-classpath --main-class=com.sun.tools.javac.Main",
	" start JavaC, the Java Compiler, instead of ImageJ",
	"--javah --only-tools-jar --headless --full-classpath --dont-patch-ij1 --pass-classpath --main-class=com.sun.tools.javah.Main",
	" start javah instead of ImageJ",
	"--javap --only-tools-jar --headless --full-classpath --dont-patch-ij1 --pass-classpath --main-class=sun.tools.javap.Main",
	" start javap instead of ImageJ",
	"--javadoc --only-tools-jar --headless --full-classpath --dont-patch-ij1 --pass-classpath --main-class=com.sun.tools.javadoc.Main",
	" start javadoc instead of ImageJ",
};

static void initialize_subcommands(void)
{
	int i;
	if (all_subcommands.size)
		return;
	for (i = 0; i < sizeof(default_subcommands) / sizeof(default_subcommands[0]); i++)
		add_subcommand(default_subcommands[i]);
}

static const char *expand_subcommand(const char *option)
{
	int i;

	initialize_subcommands();
	for (i = 0; i < all_subcommands.size; i++)
		if (!strcmp(option, all_subcommands.list[i].name))
			return all_subcommands.list[i].expanded;
	return NULL;
}

static const char *expand_subcommand_for_extension(const char *extension)
{
	int i, j;

	if (!extension)
		return NULL;

	initialize_subcommands();
	for (i = 0; i < all_subcommands.size; i++)
		for (j = 0; j < all_subcommands.list[i].extensions.size; j++)
			if (!strcmp(extension, all_subcommands.list[i].extensions.list[j]))
				return all_subcommands.list[i].expanded;
	return NULL;
}

static const char *get_file_extension(const char *path)
{
	int i = strlen(path);

	while (i)
		if (path[i - 1] == '.')
			return path + i - 1;
		else if (path[i - 1] == '/' || path[i - 1] == '\\')
			return NULL;
		else
			i--;
	return NULL;
}

__attribute__((format (printf, 1, 2)))
static int jar_exists(const char *fmt, ...)
{
	struct string string = { 0, 0, NULL };
	va_list ap;
	int result;

	va_start(ap, fmt);
	string_vaddf(&string, fmt, ap);
	result = file_exists(string.buffer);
	free(string.buffer);
	va_end(ap);

	return result;
}

/*
 * Check whether all .jar files specified in the classpath are available.
 */
static int check_subcommand_classpath(struct subcommand *subcommand)
{
	const char *expanded = subcommand->expanded;

	while (expanded && *expanded) {
		const char *space = strchr(expanded, ' ');
		if (!space)
			space = expanded + strlen(expanded);
		if (!prefixcmp(expanded, "--fiji-jar=")) {
			expanded += 11;
			if (!jar_exists("%s/%.*s", ij_path(""), (int)(space - expanded), expanded))
				return 0;
		}
		if (!prefixcmp(expanded, "--ij-jar=")) {
			expanded += 9;
			if (!jar_exists("%s/%.*s", ij_path(""), (int)(space - expanded), expanded))
				return 0;
		}
		else if (!prefixcmp(expanded, "--tools-jar") || !prefixcmp(expanded, "--only-tools-jar")) {
			const char *jre_home = get_jre_home();
			if (!jre_home || !jar_exists("%s/../lib/tools.jar", jre_home))
				return 0;
		}
		expanded = space + !!*space;
	}
	return 1;
}

static void parse_legacy_config(struct string *jvm_options)
{
	const char *p;
	p = strchr(jvm_options->buffer, '\n');
	if (p) {
		p = strchr(p + 1, '\n');
		if (p) {
			int new_length;
			p++;
			new_length = jvm_options->length - (p - jvm_options->buffer);
			memmove(jvm_options->buffer, p, new_length);
			p = strchr(jvm_options->buffer, '\n');
			if (p)
				new_length = p - jvm_options->buffer;
			if (new_length > 10 && !strncmp(jvm_options->buffer + new_length - 10, " ij.ImageJ", 10))
				new_length -= 10;
			string_set_length(jvm_options, new_length);
			return;
		}
	}
	string_set_length(jvm_options, 0);
}

const char *imagej_cfg_sentinel = "ImageJ startup properties";

static int is_modern_config(const char *text)
{
	return *text == '#' &&
		(!prefixcmp(text + 1, imagej_cfg_sentinel) ||
		 (text[1] == ' ' && !prefixcmp(text + 2, imagej_cfg_sentinel)));
}

/* Returns the number of leading whitespace characters */
static int count_leading_whitespace(const char *line)
{
	int offset = 0;

	while (line[offset] && (line[offset] == ' ' || line[offset] == '\t'))
		offset++;

	return offset;
}

static int is_end_of_line(char ch)
{
	return ch == '\r' || ch == '\n';
}

/* Returns the number of characters to skip to get to the value, or -1 if the key does not match */
static int property_line_key_matches(const char *line, const char *key)
{
	int offset = count_leading_whitespace(line);

	if (prefixcmp(line + offset, key))
		return -1;
	offset += strlen(key);

	offset += count_leading_whitespace(line + offset);

	if (line[offset++] != '=')
		return -1;

	return offset + count_leading_whitespace(line + offset);
}

static void parse_modern_config(struct string *jvm_options)
{
	int offset = 0, skip, eol;

	while (jvm_options->buffer[offset]) {
		const char *p = jvm_options->buffer + offset;

		for (eol = offset; !is_end_of_line(jvm_options->buffer[eol]); eol++)
			; /* do nothing */

		/* memory option? */
		if ((skip = property_line_key_matches(p, "maxheap.mb")) > 0) {
			const char *replacement = offset ? " -Xmx" : "-Xmx";
			string_replace_range(jvm_options, offset, offset + skip, replacement);
			eol += strlen(replacement) - skip;
			string_replace_range(jvm_options, eol, eol, "m");
			eol++;
		}
		/* jvmargs? */
		else if ((skip = property_line_key_matches(p, "jvmargs")) > 0) {
			const char *replacement = offset ? " " : "";
			string_replace_range(jvm_options, offset, offset + skip, replacement);
			eol += strlen(replacement) - skip;
		}
		/* strip it */
		else {
			string_replace_range(jvm_options, offset, eol, "");
			eol = offset;
		}

		for (offset = eol; is_end_of_line(jvm_options->buffer[eol]); eol++)
			; /* do nothing */

		if (offset != eol)
			string_replace_range(jvm_options, offset, eol, "");
	}
}

static void read_config(struct string *jvm_options)
{
	const char *path = ij_path("ImageJ.cfg");

	if (file_exists(path)) {
		read_file_as_string(path, jvm_options);
		if (is_modern_config(jvm_options->buffer))
			parse_modern_config(jvm_options);
		else
			parse_legacy_config(jvm_options);
	}
	else {
		path = ij_path("jvm.cfg");
		if (file_exists(path))
			read_file_as_string(path, jvm_options);
	}
}

static void __attribute__((__noreturn__)) usage(void)
{
	struct string subcommands = { 0, 0, NULL };
	int i;

	initialize_subcommands();
	for (i = 0; i < all_subcommands.size; i++) {
		struct subcommand *subcommand = &all_subcommands.list[i];
		if (!check_subcommand_classpath(subcommand))
			continue;
		string_addf(&subcommands, "%s\n%s", subcommand->name,
			subcommand->description.length ? subcommand->description.buffer : "");
	}

	die("Usage: %s [<Java options>.. --] [<ImageJ options>..] [<files>..]\n"
		"\n%s%s%s%s%s%s%s%s",
		main_argv[0],
		"Java options are passed to the Java Runtime, ImageJ\n"
		"options to ImageJ (or Jython, JRuby, ...).\n"
		"\n"
		"In addition, the following options are supported by Fiji:\n"
		"General options:\n"
		"--help, -h\n"
		"\tshow this help\n",
		"--dry-run\n"
		"\tshow the command line, but do not run anything\n"
		"--system\n"
		"\tdo not try to run bundled Java\n"
		"--java-home <path>\n"
		"\tspecify JAVA_HOME explicitly\n"
		"--print-java-home\n"
		"\tprint Fiji's idea of JAVA_HOME\n"
		"--print-ij-dir\n"
		"\tprint where Fiji thinks it is located\n",
#ifdef WIN32
		"--console\n"
		"\talways open an error console\n"
#endif
		"--headless\n"
		"\trun in text mode\n"
		"--ij-dir <path>\n"
		"\tset the ImageJ directory to <path> (used to find\n"
		"\t jars/, plugins/ and macros/)\n"
		"--heap, --mem, --memory <amount>\n"
		"\tset Java's heap size to <amount> (e.g. 512M)\n"
		"--class-path, --classpath, -classpath, --cp, -cp <path>\n"
		"\tappend <path> to the class path\n"
		"--jar-path, --jarpath, -jarpath <path>\n"
		"\tappend .jar files in <path> to the class path\n",
		"--pass-classpath\n"
		"\tpass -classpath <classpath> to the main() method\n"
		"--full-classpath\n"
		"\tcall the main class with the full ImageJ class path\n"
		"--dont-patch-ij1\n"
		"\tdo not try to runtime-patch ImageJ1\n"
		"--ext <path>\n"
		"\tset Java's extension directory to <path>\n"
		"--default-gc\n"
		"\tdo not use advanced garbage collector settings by default\n"
		"\t(-Xincgc -XX:PermSize=128m)\n"
		"--gc-g1\n"
		"\tuse the G1 garbage collector\n"
		"--debug-gc\n"
		"\tshow debug info about the garbage collector on stderr\n"
		"--no-splash\n"
		"\tsuppress showing a splash screen upon startup\n"
		"\n",
		"Options for ImageJ:\n"
		"--ij2\n"
		"\tstart ImageJ2 instead of ImageJ1\n"
		"--ij1\n"
		"\tstart ImageJ1\n"
		"--allow-multiple\n"
		"\tdo not reuse existing ImageJ instance\n"
		"--plugins <dir>\n"
		"\tuse <dir> to discover plugins\n"
		"--run <plugin> [<arg>]\n"
		"\trun <plugin> in ImageJ, optionally with arguments\n"
		"--compile-and-run <path-to-.java-file>\n"
		"\tcompile and run <plugin> in ImageJ\n"
		"--edit [<file>...]\n"
		"\tedit the given file in the script editor\n"
		"\n",
		"Options to run programs other than ImageJ:\n",
		subcommands.buffer,
		"--build\n"
		"\tstart a build instead of ImageJ\n"
		"\n"
		"--main-class <class name> (this is the\n"
		"\tdefault when called with a file ending in .class)\n"
		"\tstart the given class instead of ImageJ\n"
		"--retrotranslator\n"
		"\tuse Retrotranslator to support Java < 1.6\n\n");
	string_release(&subcommands);
}

static const char *skip_whitespace(const char *string)
{
	while (iswhitespace(*string))
		string++;
	return string;
}

static const char *parse_number(const char *string, unsigned int *result, int shift)
{
	char *endp;
	long value = strtol(string, &endp, 10);

	if (string == endp)
		return NULL;

	*result |= (int)(value << shift);
	return endp;
}

static unsigned int guess_java_version(void)
{
	const char *java_home = get_jre_home();

	while (java_home && *java_home) {
		if (!prefixcmp(java_home, "jdk") || !prefixcmp(java_home, "jre")) {
			unsigned int result = 0;
			const char *p = java_home + 3;

			p = parse_number(p, &result, 24);
			if (p && *p == '.')
				p = parse_number(p + 1, &result, 16);
			if (p && *p == '.')
				p = parse_number(p + 1, &result, 8);
			if (p) {
				if (*p == '_')
					p = parse_number(p + 1, &result, 0);
				return result;
			}
		}
		java_home += strcspn(java_home, "\\/") + 1;
	}
	return 0;
}

static void jvm_workarounds(struct options *options)
{
	unsigned int java_version = guess_java_version();

	if (java_version == 0x01070000 || java_version == 0x01070001) {
		add_option(options, "-XX:-UseLoopPredicate", 0);
		if (main_class && !strcmp(main_class, "sun.tools.javap.Main"))
			main_class = "com.sun.tools.javap.Main";
	}
}

/* the maximal size of the heap on 32-bit systems, in megabyte */
#ifdef WIN32
#define MAX_32BIT_HEAP 1638
#else
#define MAX_32BIT_HEAP 1920
#endif

struct string *make_memory_option(long megabytes)
{
	return string_initf("-Xmx%dm", (int)megabytes);
}

static void try_with_less_memory(long megabytes)
{
	char **new_argv;
	int i, j, found_dashdash;
	struct string *buffer;
	size_t subtract;

	/* Try again, with 25% less memory */
	if (megabytes < 0)
		return;
	subtract = megabytes >> 2;
	if (!subtract)
		return;
	megabytes -= subtract;

	buffer = string_initf("--mem=%dm", (int)megabytes);

	main_argc = main_argc_backup;
	main_argv = main_argv_backup;
	new_argv = (char **)xmalloc((3 + main_argc) * sizeof(char *));
	new_argv[0] = main_argv[0];

	j = 1;
	new_argv[j++] = xstrdup(buffer->buffer);

	/* Strip out --mem options. */
	found_dashdash = 0;
	for (i = 1; i < main_argc; i++) {
		struct string *dummy = string_init(32);
		if (!found_dashdash && !strcmp(main_argv_backup[i], "--"))
			found_dashdash = 1;
		if ((!found_dashdash || is_default_ij1_class(main_class)) &&
				(handle_one_option(&i, (const char **)main_argv, "--mem", dummy) ||
				 handle_one_option(&i, (const char **)main_argv, "--memory", dummy)))
			continue;
		new_argv[j++] = main_argv[i];
	}
	new_argv[j] = NULL;

	error("Trying with a smaller heap: %s", buffer->buffer);

	hide_splash();

#ifdef WIN32
	new_argv[0] = dos_path(new_argv[0]);
	for (i = 0; i < j; i++)
		new_argv[i] = quote_win32(new_argv[i]);
	execve(new_argv[0], (char * const *)new_argv, NULL);
#else
	execv(new_argv[0], new_argv);
#endif

	string_setf(buffer, "ERROR: failed to launch (errno=%d;%s):\n",
		errno, strerror(errno));
	for (i = 0; i < j; i++)
		string_addf(buffer, "%s ", new_argv[i]);
	string_add_char(buffer, '\n');
#ifdef WIN32
	MessageBox(NULL, buffer->buffer, "Error executing ImageJ", MB_OK);
#endif
	die("%s", buffer->buffer);
}

static int is_building(const char *target)
{
	int i;
	if (main_argc < 3 ||
			(strcmp(main_argv[1], "--build") &&
			 strcmp(main_argv[1], "--fake")))
		return 0;
	for (i = 2; i < main_argc; i++)
		if (!strcmp(main_argv[i], target))
			return 1;
	return 0;
}

static const char *maybe_substitute_ij_jar(const char *relative_path)
{
	const char *replacement = NULL;

	if (!strcmp(relative_path, "jars/jython.jar"))
		replacement = "/usr/share/java/jython.jar";
	else if (!strcmp(relative_path, "jars/clojure.jar"))
		replacement = "/usr/share/java/clojure.jar";
	else if (!strcmp(relative_path, "jars/bsh-2.0b4.jar") || !strcmp(relative_path, "jars/bsh.jar"))
		replacement = "/usr/share/java/bsh.jar";
	else if (!strcmp(relative_path, "jars/ant.jar"))
		replacement = "/usr/share/java/ant.jar";
	else if (!strcmp(relative_path, "jars/ant-launcher.jar"))
		replacement = "/usr/share/java/ant-launcher.jar";
	else if (!strcmp(relative_path, "jars/ant-nodeps.jar"))
		replacement = "/usr/share/java/ant-nodeps.jar";
	else if (!strcmp(relative_path, "jars/ant-junit.jar"))
		replacement = "/usr/share/java/ant-junit.jar";
	else if (!strcmp(relative_path, "jars/jsch-0.1.44.jar") || !strcmp(relative_path, "jars/jsch.jar"))
		replacement = "/usr/share/java/jsch.jar";
	else if (!strcmp(relative_path, "jars/javassist.jar"))
		replacement = "/usr/share/java/javassist.jar";

	if (!replacement || file_exists(ij_path(relative_path)))
		return NULL;

	return replacement;
}

/*
 * Returns the number of elements which this option spans if it is an ImageJ1 option, 0 otherwise.
 */
static int imagej1_option_count(const char *option)
{
	if (!option)
		return 0;
	if (option[0] != '-') /* file names */
		return 1;
	if (!prefixcmp(option, "-port") || !strcmp(option, "-debug"))
		return 1;
	if (!strcmp(option, "-ijpath") || !strcmp(option, "-macro") || !strcmp(option, "-eval") || !strcmp(option, "-run"))
		return 2;
	if (!strcmp(option, "-batch"))
		return 3;
	return 0;
}

const char *properties[32];

static int retrotranslator;

static struct options options;
static long megabytes = 0;
static struct string buffer, buffer2, arg, plugin_path, ext_option;
static int jdb, advanced_gc = 1, debug_gc;
static int allow_multiple, skip_class_launcher, full_class_path;

static int handle_one_option2(int *i, int argc, const char **argv)
{
	if (!strcmp(argv[*i], "--dry-run"))
		options.debug++;
	else if (handle_one_option(i, argv, "--java-home", &arg)) {
		absolute_java_home = xstrdup(arg.buffer);
		setenv_or_exit("JAVA_HOME", xstrdup(arg.buffer), 1);
	}
	else if (!strcmp(argv[*i], "--system"))
		options.use_system_jvm++;
	else if (!strcmp(argv[*i], "--console"))
#ifdef WIN32
		open_win_console();
#else
		; /* ignore */
#endif
	else if (!strcmp(argv[*i], "--jdb")) {
		add_tools_jar(&options);
		add_launcher_option(&options, "-jdb", NULL);
	}
	else if (!strcmp(argv[*i], "--allow-multiple"))
		allow_multiple = 1;
	else if (handle_one_option(i, argv, "--plugins", &arg))
		string_addf(&plugin_path, "-Dplugins.dir=%s", arg.buffer);
	else if (handle_one_option(i, argv, "--run", &arg)) {
		string_replace(&arg, '_', ' ');
		if (*i + 1 < argc && argv[*i + 1][0] != '-')
			string_addf(&arg, "\", \"%s", argv[++(*i)]);
		add_option(&options, "-eval", 1);
		string_setf(&buffer, "run(\"%s\");", arg.buffer);
		add_option_string(&options, &buffer, 1);
		headless_argc++;
	}
	else if (handle_one_option(i, argv, "--compile-and-run", &arg)) {
		add_option(&options, "-eval", 1);
		string_setf(&buffer, "run(\"Refresh Javas\", \"%s \");",
			make_absolute_path(arg.buffer));
		add_option_string(&options, &buffer, 1);
		headless_argc++;
	}
	else if (*i == argc - 1 && !strcmp(argv[*i], "--edit")) {
		add_option(&options, "-eval", 1);
		add_option(&options, "run(\"Script Editor\");", 1);
	}
	else if (handle_one_option(i, argv, "--edit", &arg))
		for (;;) {
			add_option(&options, "-eval", 1);
			if (*arg.buffer && strncmp(arg.buffer, "class:", 6)) {
				string_set(&arg, make_absolute_path(arg.buffer));
				string_escape(&arg, "\\");
			}
			string_setf(&buffer, "run(\"Script Editor\", \"%s\");", arg.buffer);
			add_option_string(&options, &buffer, 1);
			if (*i + 1 >= argc)
				break;
			string_setf(&arg, "%s", argv[++(*i)]);
		}
	else if (handle_one_option(i, argv, "--heap", &arg) ||
			handle_one_option(i, argv, "--mem", &arg) ||
			handle_one_option(i, argv, "--memory", &arg))
		megabytes = parse_memory(arg.buffer);
	else if (!strcmp(argv[*i], "--headless"))
		headless = 1;
	else if (handle_one_option(i, argv, "--main-class", &arg)) {
		add_launcher_option(&options, "-classpath", ".");
		main_class = xstrdup(arg.buffer);
	}
	else if (handle_one_option(i, argv, "--jar", &arg)) {
		add_launcher_option(&options, "-classpath", arg.buffer);
		main_class = "imagej.JarLauncher";
		add_option_string(&options, &arg, 1);
	}
	else if (handle_one_option(i, argv, "--class-path", &arg) ||
			handle_one_option(i, argv, "--classpath", &arg) ||
			handle_one_option(i, argv, "-classpath", &arg) ||
			handle_one_option(i, argv, "--cp", &arg) ||
			handle_one_option(i, argv, "-cp", &arg))
		add_launcher_option(&options, "-classpath", arg.buffer);
	else if (handle_one_option(i, argv, "--fiji-jar", &arg) || handle_one_option(i, argv, "--ij-jar", &arg)) {
		const char *path = maybe_substitute_ij_jar(arg.buffer);
		if (path)
			add_launcher_option(&options, "-classpath", path);
		else
			add_launcher_option(&options, "-ijclasspath", arg.buffer);
	}
	else if (handle_one_option(i, argv, "--jar-path", &arg) ||
			handle_one_option(i, argv, "--jarpath", &arg) ||
			handle_one_option(i, argv, "-jarpath", &arg))
		add_launcher_option(&options, "-jarpath", arg.buffer);
	else if (!strcmp(argv[*i], "--full-classpath"))
		full_class_path = 1;
	else if (!strcmp(argv[*i], "--freeze-classloader"))
		add_launcher_option(&options, "-freeze-classloader", NULL);
	else if (handle_one_option(i, argv, "--ext", &arg)) {
		string_append_path_list(&ext_option, arg.buffer);
	}
	else if (!strcmp(argv[*i], "--ij2") || !strcmp(argv[*i], "--imagej"))
		main_class = default_main_class;
	else if (!strcmp(argv[*i], "--ij1") || !strcmp(argv[*i], "--legacy"))
		main_class = default_fiji1_class;
	else if (!strcmp(argv[*i], "--build") ||
			!strcmp(argv[*i], "--fake")) {
		const char *fake_jar, *precompiled_fake_jar;
#ifdef WIN32
		open_win_console();
#endif
		skip_class_launcher = 1;
		headless = 1;
		fake_jar = ij_path("jars/fake.jar");
		precompiled_fake_jar = ij_path("precompiled/fake.jar");
		if (run_precompiled || !file_exists(fake_jar) ||
				file_is_newer(precompiled_fake_jar, fake_jar))
			fake_jar = precompiled_fake_jar;
		if (file_is_newer(ij_path("src-plugins/fake/fiji/build/Fake.java"), fake_jar) &&
				!is_building("jars/fake.jar"))
			error("Warning: jars/fake.jar is not up-to-date");
		string_set_length(&arg, 0);
		string_addf(&arg, "-Djava.class.path=%s", fake_jar);
		add_option_string(&options, &arg, 0);
		main_class = "fiji.build.Fake";
	}
	else if (!strcmp(argv[*i], "--tools-jar"))
		add_tools_jar(&options);
	else if (!strcmp(argv[*i], "--only-tools-jar")) {
		add_tools_jar(&options);
		add_launcher_option(&options, "-freeze-classloader", NULL);
	}
	else if (!strcmp(argv[*i], "--dont-patch-ij1"))
		add_option(&options, "-Dpatch.ij1=false", 0);
	else if (!strcmp(argv[*i], "--pass-classpath"))
		add_launcher_option(&options, "-pass-classpath", NULL);
	else if (!strcmp(argv[*i], "--retrotranslator") ||
			!strcmp(argv[*i], "--retro"))
		retrotranslator = 1;
	else if (handle_one_option(i, argv, "--fiji-dir", &arg))
		ij_dir = xstrdup(arg.buffer);
	else if (handle_one_option(i, argv, "--ij-dir", &arg))
		ij_dir = xstrdup(arg.buffer);
	else if (!strcmp("--print-ij-dir", argv[*i])) {
		printf("%s\n", ij_dir);
		exit(0);
	}
	else if (!strcmp("--print-java-home", argv[*i])) {
		const char *java_home = get_java_home();
		printf("%s\n", !java_home ? "" : java_home);
		exit(0);
	}
	else if (!strcmp("--default-gc", argv[*i]))
		advanced_gc = 0;
	else if (!strcmp("--gc-g1", argv[*i]) ||
			!strcmp("--g1", argv[*i]))
		advanced_gc = 2;
	else if (!strcmp("--debug-gc", argv[*i]))
		debug_gc = 1;
	else if (!strcmp("--no-splash", argv[*i]))
		no_splash = 1;
	else if (!strcmp("--help", argv[*i]) ||
			!strcmp("-h", argv[*i]))
		usage();
	else
		return 0;
	return 1;
}

static void handle_commandline(const char *line)
{
	int i, alloc = 32, argc = 0;
	char **argv = xmalloc(alloc * sizeof(char *));
	const char *current;

	current = line = skip_whitespace(line);
	if (!*current)
		return;

	while (*(++line))
		if (iswhitespace(*line)) {
			if (argc + 2 >= alloc) {
				alloc = (16 + alloc) * 3 / 2;
				argv = xrealloc(argv, alloc * sizeof(char *));
			}
			argv[argc++] = xstrndup(current, line - current);
			current = line = skip_whitespace(line + 1);
		}

	if (current != line)
		argv[argc++] = xstrndup(current, line - current);

	for (i = 0; i < argc; i++)
		if (!handle_one_option2(&i, argc, (const char **)argv))
			die("Unhandled option: %s", argv[i]);

	while (argc > 0)
		free(argv[--argc]);
	free(argv);
}

static void parse_command_line(void)
{
	struct string *jvm_options = string_init(32);
	struct string *default_arguments = string_init(32);
	struct string *java_library_path = string_init(32);
	struct string *library_base_path;
	int dashdash = 0;
	int count = 1, i;

#ifdef WIN32
#define EXE_EXTENSION ".exe"
#else
#define EXE_EXTENSION
#endif

	string_setf(&buffer, "%s/ij" EXE_EXTENSION, ij_dir);
	string_setf(&buffer2, "%s/ij.c", ij_dir);
	if (file_exists(ij_path("ImageJ" EXE_EXTENSION)) &&
			file_is_newer(ij_path("ImageJ.c"), ij_path("ImageJ" EXE_EXTENSION)) &&
			!is_building("ImageJ"))
		error("Warning: your ImageJ executable is not up-to-date");

#ifdef __linux__
	string_append_path_list(java_library_path, getenv("LD_LIBRARY_PATH"));
#endif
#ifdef __APPLE__
	string_append_path_list(java_library_path, getenv("DYLD_LIBRARY_PATH"));
#endif

	if (get_platform() != NULL) {
		struct string *buffer = string_initf("%s/%s", ij_path("lib"), get_platform());
		string_append_path_list(java_library_path, buffer->buffer);
		string_release(buffer);
	}

	library_base_path = string_copy(ij_path("lib"));
	detect_library_path(java_library_path, library_base_path);
	string_release(library_base_path);

#ifdef WIN32
	if (java_library_path->length) {
		struct string *new_path = string_initf("%s%s%s",
			getenv("PATH"), PATH_SEP, java_library_path->buffer);
		setenv("PATH", new_path->buffer, 1);
		string_release(new_path);
	}
#endif

	memset(&options, 0, sizeof(options));

#ifdef __APPLE__
	/* When double-clicked Finder adds a psn argument. */
	if (main_argc > 1 && ! strncmp(main_argv[1], "-psn_", 5)) {
		/*
		 * Reset main_argc so that ImageJ won't try to open
		 * that empty argument as a file (the root directory).
		 */
		main_argc = 1;
		/*
		 * Additionally, change directory to the ij dir to emulate
		 * the behavior of the regular ImageJ application which does
		 * not start up in the filesystem root.
		 */
		chdir(ij_dir);
	}

	if (!get_fiji_bundle_variable("heap", &arg) ||
			!get_fiji_bundle_variable("mem", &arg) ||
			!get_fiji_bundle_variable("memory", &arg)) {
		if (!strcmp("auto", arg.buffer))
			megabytes = 0;
		else
			megabytes = parse_memory(arg.buffer);
	}
	if (!get_fiji_bundle_variable("system", &arg) &&
			atol((&arg)->buffer) > 0)
		options.use_system_jvm++;
	if (get_fiji_bundle_variable("ext", &ext_option)) {
		const char *java_home = get_java_home();
		string_setf(&ext_option, "%s%s"
			"/Library/Java/Extensions:"
			"/System/Library/Java/Extensions:"
			"/System/Library/Frameworks/JavaVM.framework/Home/lib/ext",
			java_home ? java_home : "", java_home ? "/Home/lib/ext:" : "");
	}
	if (!get_fiji_bundle_variable("allowMultiple", &arg))
		allow_multiple = parse_bool((&arg)->buffer);
	get_fiji_bundle_variable("JVMOptions", jvm_options);
	get_fiji_bundle_variable("DefaultArguments", default_arguments);
#else
	read_config(jvm_options);
#endif

	if (jvm_options->length)
		add_options(&options, jvm_options->buffer, 0);

	for (i = 1; i < main_argc; i++)
		if (!strcmp(main_argv[i], "--") && !dashdash)
			dashdash = count;
		else if (dashdash && main_class &&
				!is_default_ij1_class(main_class))
			main_argv[count++] = main_argv[i];
		else if (handle_one_option2(&i, main_argc, (const char **)main_argv))
			; /* ignore */
		else {
			const char *expanded = expand_subcommand(main_argv[i]);
			if (expanded)
				handle_commandline(expanded);
			else
				main_argv[count++] = main_argv[i];
		}

	main_argc = count;

#ifdef WIN32
	/* Windows automatically adds the path of the executable to PATH */
	const char *jre_home = get_jre_home();
	if (jre_home) {
		struct string *path = string_initf("%s;%s/bin",
			getenv("PATH"), jre_home);
		setenv_or_exit("PATH", path->buffer, 1);
		string_release(path);
	}
#endif
	if (!headless &&
#ifdef __APPLE__
			!CGSessionCopyCurrentDictionary()
#elif defined(__linux__)
			!getenv("DISPLAY")
#else
			0
#endif
			) {
		error("No GUI detected.  Falling back to headless mode.");
		headless = 1;
	}

	if (ext_option.length) {
		string_setf(&buffer, "-Djava.ext.dirs=%s", ext_option.buffer);
		add_option_string(&options, &buffer, 0);
	}

	/* Avoid Jython's huge startup cost: */
	add_option(&options, "-Dpython.cachedir.skip=true", 0);
	if (!plugin_path.length)
		string_setf(&plugin_path, "-Dplugins.dir=%s", ij_dir);
	add_option(&options, plugin_path.buffer, 0);

	/* If arguments don't set the memory size, set it after available memory. */
	if (megabytes == 0 && !has_memory_option(&options.java_options)) {
		megabytes = (long)(get_memory_size(0) >> 20);
		/* 0.75x, but avoid multiplication to avoid overflow */
		megabytes -= megabytes >> 2;
		if (sizeof(void *) == 4 && megabytes > MAX_32BIT_HEAP)
			megabytes = MAX_32BIT_HEAP;
	}

	if (megabytes > 0)
		add_option(&options, make_memory_option(megabytes)->buffer, 0);

	if (headless)
		add_option(&options, "-Djava.awt.headless=true", 0);

	if (is_ipv6_broken())
		add_option(&options, "-Djava.net.preferIPv4Stack=true", 0);

	jvm_workarounds(&options);

	if (advanced_gc == 1) {
		add_option(&options, "-Xincgc", 0);
		add_option(&options, "-XX:PermSize=128m", 0);
	}
	else if (advanced_gc == 2) {
		add_option(&options, "-XX:PermSize=128m", 0);
		add_option(&options, "-XX:+UseCompressedOops", 0);
		add_option(&options, "-XX:+UnlockExperimentalVMOptions", 0);
		add_option(&options, "-XX:+UseG1GC", 0);
		add_option(&options, "-XX:+G1ParallelRSetUpdatingEnabled", 0);
		add_option(&options, "-XX:+G1ParallelRSetScanningEnabled", 0);
		add_option(&options, "-XX:NewRatio=5", 0);
	}

	if (debug_gc)
		add_option(&options, "-verbose:gc", 0);

	if (!main_class) {
		int index = dashdash ? dashdash : 1;
		const char *first = main_argv[index];
		int len = main_argc > index ? strlen(first) : 0;
		const char *expanded;

		if (len > 1 && !strncmp(first, "--", 2))
			len = 0;
		if (len > 3 && (expanded = expand_subcommand_for_extension(get_file_extension(first))))
			handle_commandline(expanded);
		else if (len > 6 && !strcmp(first + len - 6, ".class")) {
			struct string *dotted = string_copy(first);
			add_launcher_option(&options, "-classpath", ".");
			string_replace(dotted, '/', '.');
			string_set_length(dotted, len - 6);
			main_class = xstrdup(dotted->buffer);
			main_argv++;
			main_argc--;
			string_release(dotted);
		}
		else
			main_class = default_main_class;
	}

	maybe_reexec_with_correct_lib_path();

	if (!options.debug && !options.use_system_jvm && !headless && is_default_ij1_class(main_class))
		show_splash();

	/* set up class path */
	if (full_class_path || !strcmp(default_main_class, main_class)) {
		add_launcher_option(&options, "-ijjarpath", "jars");
		add_launcher_option(&options, "-ijjarpath", "plugins");
	}
	else if (is_default_ij1_class(main_class))
		add_launcher_option(&options, "-ijclasspath", "jars/ij.jar");

	if (default_arguments->length)
		add_options(&options, default_arguments->buffer, 1);

	if (!strcmp(main_class, "org.apache.tools.ant.Main"))
		add_java_home_to_path();

	if (is_default_ij1_class(main_class)) {
		if (allow_multiple)
			add_option(&options, "-port0", 1);
		else
			add_option(&options, "-port7", 1);
		add_option(&options, "-Dsun.java.command=ImageJ", 0);
	}

	/* If there is no -- but some options unknown to IJ1, DWIM it. */
	if (!dashdash && is_default_ij1_class(main_class)) {
		for (i = 1; i < main_argc; i++) {
			int count = imagej1_option_count(main_argv[i]);
			if (!count) {
				dashdash = main_argc;
				break;
			}
			i += count - 1;
		}
	}

	if (dashdash) {
		int is_imagej1 = is_default_ij1_class(main_class);

		for (i = 1; i < dashdash; ) {
			int count = is_imagej1 ? imagej1_option_count(main_argv[i]) : 0;
			if (!count)
				add_option(&options, main_argv[i++], 0);
			else
				while (count-- && i < dashdash)
					add_option(&options, main_argv[i++], 1);
		}
		main_argv += dashdash - 1;
		main_argc -= dashdash - 1;
	}

	/* handle "--headless script.ijm" gracefully */
	if (headless && is_default_ij1_class(main_class)) {
		if (main_argc + headless_argc < 2) {
			error("--headless without a parameter?");
			if (!options.debug)
				exit(1);
		}
		if (main_argc > 1 && *main_argv[1] != '-')
			add_option(&options, "-batch", 1);
	}

	if (jdb)
		add_launcher_option(&options, "-jdb", NULL);

	if (retrotranslator)
		add_launcher_option(&options, "-retrotranslator", NULL);

	for (i = 1; i < main_argc; i++)
		add_option(&options, main_argv[i], 1);

	i = 0;
	properties[i++] = "imagej.dir";
	properties[i++] =  ij_dir,
	properties[i++] = "ij.dir";
	properties[i++] =  ij_dir,
	properties[i++] = "fiji.dir";
	properties[i++] =  ij_dir,
	properties[i++] = "fiji.defaultLibPath";
	properties[i++] = default_library_path;
	properties[i++] = "fiji.executable";
	properties[i++] = main_argv0;
	properties[i++] = "ij.executable";
	properties[i++] = main_argv0;
	properties[i++] = "java.library.path";
	properties[i++] = java_library_path->buffer;
#ifdef WIN32
	properties[i++] = "sun.java2d.noddraw";
	properties[i++] = "true";
#endif
	properties[i++] = NULL;

	if (i > sizeof(properties) / sizeof(properties[0]))
		die ("Too many properties: %d", i);

	keep_only_one_memory_option(&options.java_options);

	if (!skip_class_launcher && strcmp(main_class, "org.apache.tools.ant.Main")) {
		struct string *string = string_initf("-Djava.class.path=%s", ij_path("jars/ij-launcher.jar"));
		add_option_string(&options, string, 0);
		add_launcher_option(&options, main_class, NULL);
		prepend_string_array(&options.ij_options, &options.launcher_options);
		main_class = "imagej.ClassLauncher";
	}
	else {
		struct string *class_path = string_init(32);
		const char *sep = "-Djava.class.path=";
		int i;

		for (i = 0; i < options.launcher_options.nr; i++) {
			const char *option = options.launcher_options.list[i];
			if (sep)
				string_append(class_path, sep);
			if (!strcmp(option, "-ijclasspath"))
				string_append(class_path, ij_path(options.launcher_options.list[++i]));
			else if (!strcmp(option, "-classpath"))
				string_append(class_path, options.launcher_options.list[++i]);
			else
				die ("Without ij-launcher, '%s' cannot be handled", option);
			sep = PATH_SEP;
		}

		if (class_path->length)
			add_option_string(&options, class_path, 0);
		string_release(class_path);
	}

	if (options.debug) {
		for (i = 0; properties[i]; i += 2) {
			if (!properties[i] || !properties[i + 1])
				continue;
			string_setf(&buffer, "-D%s=%s", properties[i], properties[i + 1]);
			add_option_string(&options, &buffer, 0);
		}

		show_commandline(&options);
		exit(0);
	}

}

static int start_ij(void)
{
	JavaVM *vm;
	JavaVMInitArgs args;
	JNIEnv *env;
	struct string *buffer = string_init(32);
	int i;

	/* Handle update/ */
	update_all_files();

	memset(&args, 0, sizeof(args));
	/* JNI_VERSION_1_4 is used on Mac OS X to indicate 1.4.x and later */
	args.version = JNI_VERSION_1_4;
	args.options = prepare_java_options(&options.java_options);
	args.nOptions = options.java_options.nr;
	args.ignoreUnrecognized = JNI_FALSE;

	if (!get_jre_home() || options.use_system_jvm)
		env = NULL;
	else {
		int result = create_java_vm(&vm, (void **)&env, &args);
		if (result == JNI_ENOMEM) {
			if (!megabytes) {
				const char *option = has_memory_option(&options.java_options);
				if (!option || prefixcmp(option, "-Xm") || !option[3])
					die ("Out of memory, could not determine heap size!");
				megabytes = parse_memory(option + 4);
			}
			try_with_less_memory(megabytes);
			die("Out of memory!");
		}
		if (result) {
			if (result != 2) {
				error("Warning: falling back to System JVM");
				unsetenv("JAVA_HOME");
			}
			env = NULL;
		} else {
			const char *java_home = get_java_home();
			if (java_home) {
				string_setf(buffer, "-Djava.home=%s", java_home);
				prepend_string_copy(&options.java_options, buffer->buffer);
			}
		}
	}

	if (env) {
		jclass instance;
		jmethodID method;
		jobjectArray args;
		struct string *slashed = string_copy(main_class);

		for (i = 0; properties[i]; i += 2)
			set_property(env, properties[i], properties[i + 1]);

		string_replace(slashed, '.', '/');
		if (!(instance = (*env)->FindClass(env, slashed->buffer))) {
			(*env)->ExceptionDescribe(env);
			die("Could not find %s", slashed->buffer);
		}
		else if (!(method = (*env)->GetStaticMethodID(env, instance,
				"main", "([Ljava/lang/String;)V"))) {
			(*env)->ExceptionDescribe(env);
			die("Could not find main method of %s", slashed->buffer);
		}
		string_release(slashed);

		args = prepare_ij_options(env, &options.ij_options);
		(*env)->CallStaticVoidMethodA(env, instance,
				method, (jvalue *)&args);
		hide_splash();
		if ((*vm)->DetachCurrentThread(vm))
			error("Could not detach current thread");
		/* This does not return until ImageJ exits */
		(*vm)->DestroyJavaVM(vm);
	} else {
		/* fall back to system-wide Java */
		const char *java_home_env;
#ifdef __APPLE__
		struct string *icon_option;
		/*
		 * On MacOSX, one must (stupidly) fork() before exec() to
		 * clean up some pthread state somehow, otherwise the exec()
		 * will fail with "Operation not supported".
		 */
		if (fork())
			exit(0);

		add_option(&options, "-Xdock:name=ImageJ", 0);
		icon_option = string_copy("-Xdock:icon=");
		append_icon_path(icon_option);
		add_option_string(&options, icon_option, 0);
		string_release(icon_option);
#endif

		for (i = 0; properties[i]; i += 2) {
			if (!properties[i] || !properties[i + 1])
				continue;
			string_setf(buffer, "-D%s=%s", properties[i], properties[i + 1]);
			add_option_string(&options, buffer, 0);
		}

		/* fall back to system-wide Java */
		add_option_copy(&options, main_class, 0);
		append_string_array(&options.java_options, &options.ij_options);
		append_string(&options.java_options, NULL);
		prepend_string(&options.java_options, strdup(get_java_command()));

		string_set(buffer, get_java_command());
		java_home_env = getenv("JAVA_HOME");
		if (java_home_env && strlen(java_home_env) > 0) {
			error("Found that JAVA_HOME was: '%s'", java_home_env);
			string_setf(buffer, "%s/bin/%s", java_home_env, get_java_command());
		}
		options.java_options.list[0] = buffer->buffer;
		hide_splash();
#ifndef WIN32
		if (execvp(buffer->buffer, options.java_options.list))
			error("Could not launch system-wide Java (%s)", strerror(errno));
#else
		if (console_opened && !console_attached)
			sleep(5); /* Sleep 5 seconds */

		for (i = 0; i < options.java_options.nr - 1; i++)
			options.java_options.list[i] =
				quote_win32(options.java_options.list[i]);
		STARTUPINFO startup_info;
		PROCESS_INFORMATION process_info;
		const char *java = find_in_path(console_opened || console_attached ? "java" : "javaw");
		struct string *cmdline = string_initf("java");

		if (!java)
			die("Could not find java.exe in PATH!");

		memset(&startup_info, 0, sizeof(startup_info));
		startup_info.cb = sizeof(startup_info);

		memset(&process_info, 0, sizeof(process_info));

		for (i = 1; i < options.java_options.nr - 1; i++)
			string_addf(cmdline, " %s", options.java_options.list[i]);
		if (CreateProcess(java, cmdline->buffer, NULL, NULL, TRUE, NORMAL_PRIORITY_CLASS, NULL, NULL, &startup_info, &process_info)) {
			DWORD exit_code;
			WaitForSingleObject(process_info.hProcess, INFINITE);
			if (GetExitCodeProcess(process_info.hProcess, &exit_code) && exit_code)
				exit(exit_code);
			return 0;
		}

		char message[16384];
		int off = sprintf(message, "Error: '%s' while executing\n\n",
				strerror(errno));
		for (i = 0; options.java_options.list[i]; i++)
			off += sprintf(message + off, "'%s'\n",
					options.java_options.list[i]);
		MessageBox(NULL, message, "Error", MB_OK);
#endif
		exit(1);
	}
	return 0;
}

#ifdef __APPLE__
static void append_icon_path(struct string *str)
{
	/*
	 * Check if we're launched from within an Application bundle or
	 * command line.  If from a bundle, Fiji.app should be in the path.
	 */
	string_append(str, ij_path(!suffixcmp(ij_dir, strlen(ij_dir), "Fiji.app") ?
		"Contents/Resources/Fiji.icns" : "images/Fiji.icns"));
}

#include <sys/types.h>
#include <sys/sysctl.h>
#include <mach/machine.h>
#include <unistd.h>
#include <sys/param.h>
#include <string.h>

static struct string *convert_cfstring(CFStringRef ref, struct string *buffer)
{
	string_ensure_alloc(buffer, (int)CFStringGetLength(ref) * 6);
	if (!CFStringGetCString((CFStringRef)ref, buffer->buffer, buffer->alloc, kCFStringEncodingUTF8))
		string_set_length(buffer, 0);
	else
		buffer->length = strlen(buffer->buffer);
	return buffer;
}

static struct string *resolve_alias(CFDataRef ref, struct string *buffer)
{
	if (!ref)
		string_set_length(buffer, 0);
	else {
		CFRange range = { 0, CFDataGetLength(ref) };
		if (range.length <= 0) {
			string_set_length(buffer, 0);
			return buffer;
		}
		AliasHandle alias = (AliasHandle)NewHandle(range.length);
		CFDataGetBytes(ref, range, (void *)*alias);

		string_ensure_alloc(buffer, 1024);
		FSRef fs;
		Boolean changed;
		if (FSResolveAlias(NULL, alias, &fs, &changed) == noErr)
			FSRefMakePath(&fs, (unsigned char *)buffer->buffer, buffer->alloc);
		else {
			CFStringRef string;
			if (FSCopyAliasInfo(alias, NULL, NULL, &string, NULL, NULL) == noErr) {
				convert_cfstring(string, buffer);
				CFRelease(string);
			}
			else
				string_set_length(buffer, 0);
		}
		buffer->length = strlen(buffer->buffer);

		DisposeHandle((Handle)alias);
	}

	return buffer;
}

static int is_intel(void)
{
	int mib[2] = { CTL_HW, HW_MACHINE };
	char result[128];
	size_t len = sizeof(result);;

	if (sysctl(mib, 2, result, &len, NULL, 0) < 0)
		return 0;
	return !strcmp(result, "i386") || !strncmp(result, "x86", 3);
}

static int force_32_bit_mode(const char *argv0)
{
	int result = 0;
	struct string *buffer = string_initf("%s/%s", getenv("HOME"),
		"Library/Preferences/com.apple.LaunchServices.plist");
	if (!buffer)
		return 0;

	CFURLRef launchServicesURL =
		CFURLCreateFromFileSystemRepresentation(kCFAllocatorDefault,
		(unsigned char *)buffer->buffer, buffer->length, 0);
	if (!launchServicesURL)
		goto release_buffer;

	CFDataRef data;
	SInt32 errorCode;
	if (!CFURLCreateDataAndPropertiesFromResource(kCFAllocatorDefault,
			launchServicesURL, &data, NULL, NULL, &errorCode))
		goto release_url;

	CFDictionaryRef dict;
	CFStringRef errorString;
	dict = (CFDictionaryRef)CFPropertyListCreateFromXMLData(kCFAllocatorDefault,
		data, kCFPropertyListImmutable, &errorString);
	if (!dict || errorString)
		goto release_data;


	CFDictionaryRef arch64 = (CFDictionaryRef)CFDictionaryGetValue(dict,
		CFSTR("LSArchitecturesForX86_64"));
	if (!arch64)
		goto release_dict;

	CFArrayRef app = (CFArrayRef)CFDictionaryGetValue(arch64, CFSTR("org.fiji"));
	if (!app)
		goto release_dict;

	int i, count = (int)CFArrayGetCount(app);
	for (i = 0; i < count; i += 2) {
		convert_cfstring((CFStringRef)CFArrayGetValueAtIndex(app, i + 1), buffer);
		if (strcmp(buffer->buffer, "i386"))
			continue;
		resolve_alias((CFDataRef)CFArrayGetValueAtIndex(app, i), buffer);
		if (!strcmp(buffer->buffer, ij_dir)) {
			result = 1;
			break;
		}
	}
release_dict:
	CFRelease(dict);
release_data:
	CFRelease(data);
release_url:
	CFRelease(launchServicesURL);
release_buffer:
	string_release(buffer);
	return result;
}

static void set_path_to_JVM(void)
{
	/*
	 * MacOSX specific stuff for system java
	 * -------------------------------------
	 * Non-macosx works but places java into separate pid,
	 * which causes all kinds of strange behaviours (app can
	 * launch multiple times, etc).
	 *
	 * Search for system wide java >= 1.5
	 * and if found, launch ImageJ with the system wide java.
	 * This is an adaptation from simple.c from Apple's
	 * simpleJavaLauncher code.
	 */

	/* Look for the JavaVM bundle using its identifier. */
	CFBundleRef JavaVMBundle =
		CFBundleGetBundleWithIdentifier(CFSTR("com.apple.JavaVM"));

	if (!JavaVMBundle)
		return;

	/* Get a path for the JavaVM bundle. */
	CFURLRef JavaVMBundleURL = CFBundleCopyBundleURL(JavaVMBundle);
	CFRelease(JavaVMBundle);
	if (!JavaVMBundleURL)
		return;

	/* Append to the path the Versions Component. */
	CFURLRef JavaVMBundlerVersionsDirURL =
		CFURLCreateCopyAppendingPathComponent(kCFAllocatorDefault,
				JavaVMBundleURL, CFSTR("Versions"), 1);
	CFRelease(JavaVMBundleURL);
	if (!JavaVMBundlerVersionsDirURL)
		return;

	/* Append to the path the target JVM's Version. */
	CFURLRef TargetJavaVM = NULL;
	CFStringRef targetJVM; /* Minimum Java5. */

	/* TODO: disable this test on 10.6+ */
	/* Try 1.6 only with 64-bit */
	if (is_intel() && sizeof(void *) > 4) {
		targetJVM = CFSTR("1.6");
		TargetJavaVM =
		CFURLCreateCopyAppendingPathComponent(kCFAllocatorDefault,
				JavaVMBundlerVersionsDirURL, targetJVM, 1);
	}

	if (!TargetJavaVM) {
		retrotranslator = 1;
		targetJVM = CFSTR("1.5");
		TargetJavaVM =
		CFURLCreateCopyAppendingPathComponent(kCFAllocatorDefault,
				JavaVMBundlerVersionsDirURL, targetJVM, 1);
	}

	CFRelease(JavaVMBundlerVersionsDirURL);
	if (!TargetJavaVM)
		return;

	UInt8 pathToTargetJVM[PATH_MAX] = "";
	Boolean result = CFURLGetFileSystemRepresentation(TargetJavaVM, 1,
				pathToTargetJVM, PATH_MAX);
	CFRelease(TargetJavaVM);
	if (!result)
		return;

	/*
	 * Check to see if the directory, or a symlink for the target
	 * JVM directory exists, and if so set the environment
	 * variable JAVA_JVM_VERSION to the target JVM.
	 */
	if (access((const char *)pathToTargetJVM, R_OK))
		return;

	/*
	 * Ok, the directory exists, so now we need to set the
	 * environment var JAVA_JVM_VERSION to the CFSTR targetJVM.
	 *
	 * We can reuse the pathToTargetJVM buffer to set the environment
	 * varable.
	 */
	if (CFStringGetCString(targetJVM, (char *)pathToTargetJVM,
				PATH_MAX, kCFStringEncodingUTF8))
		setenv("JAVA_JVM_VERSION",
				(const char *)pathToTargetJVM, 1);

}

static int get_fiji_bundle_variable(const char *key, struct string *value)
{
	/*
	 * Reading the command line options from the Info.plist file in the
	 * Application bundle.
	 *
	 * This routine expects a separate dictionary for fiji with the
	 * options from the command line as keys.
	 *
	 * If Info.plist is not present (i.e. if started from the cmd-line),
	 * the whole thing will be just skipped.
	 *
	 * Example: Setting the java heap to 1024m
	 * <key>fiji</key>
	 * <dict>
	 *	<key>heap</key>
	 *	<string>1024</string>
	 * </dict>
	 */

	static CFDictionaryRef fijiInfoDict;
	static int initialized = 0;

	if (!initialized) {
		initialized = 1;

		/* Get the main bundle for the app. */
		CFBundleRef fijiBundle = CFBundleGetMainBundle();
		if (!fijiBundle)
			return -1;

		/* Get an instance of the non-localized keys. */
		CFDictionaryRef bundleInfoDict =
			CFBundleGetInfoDictionary(fijiBundle);
		if (!bundleInfoDict)
			return -2;

		fijiInfoDict = (CFDictionaryRef)
			CFDictionaryGetValue(bundleInfoDict, CFSTR("fiji"));
	}

	if (!fijiInfoDict)
		return -3;

	CFStringRef key_ref =
		CFStringCreateWithCString(NULL, key,
			kCFStringEncodingMacRoman);
	if (!key_ref)
		return -4;

	CFStringRef propertyString = (CFStringRef)
		CFDictionaryGetValue(fijiInfoDict, key_ref);
	CFRelease(key_ref);
	if (!propertyString)
		return -5;

	const char *c_string = CFStringGetCStringPtr(propertyString, kCFStringEncodingMacRoman);
	if (!c_string)
		return -6;

	string_set_length(value, 0);
	string_append(value, c_string);

	return 0;
}

/* MacOSX needs to run Java in a new thread, AppKit in the main thread. */

static void dummy_call_back(void *info) {
}

static void *start_ij_aux(void *dummy)
{
	exit(start_ij());
}

static int start_ij_macosx(void)
{
	struct string *env_key, *icon_path;

	/* set the Application's name */
	env_key = string_initf("APP_NAME_%d", (int)getpid());
	setenv(env_key->buffer, "ImageJ", 1);

	/* set the Dock icon */
	string_setf(env_key, "APP_ICON_%d", (int)getpid());
	icon_path = string_init(32);
	append_icon_path(icon_path);
	setenv(env_key->buffer, icon_path->buffer, 1);

	string_release(env_key);
	string_release(icon_path);

	pthread_t thread;
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

	/* Start the thread that we will start the JVM on. */
	pthread_create(&thread, &attr, start_ij_aux, NULL);
	pthread_attr_destroy(&attr);

	CFRunLoopSourceContext context;
	memset(&context, 0, sizeof(context));
	context.perform = &dummy_call_back;

	CFRunLoopSourceRef ref = CFRunLoopSourceCreate(NULL, 0, &context);
	CFRunLoopAddSource (CFRunLoopGetCurrent(), ref, kCFRunLoopCommonModes);
	CFRunLoopRun();

	return 0;
}
#define start_ij start_ij_macosx

/*
 * Them stupid Apple software designers -- in their infinite wisdom -- added
 * 64-bit support to Tiger without really supporting it.
 *
 * As a consequence, a universal binary will be executed in 64-bit mode on
 * a x86_64 machine, even if neither CoreFoundation nor Java can be linked,
 * and sure enough, the executable will crash.
 *
 * It does not even reach main(), so we have to have a binary that does _not_
 * provide 64-bit support, detect if it is actually on Leopard, and execute
 * another binary in that case that _does_ provide 64-bit support, even if
 * we'd rather meet the Apple software designers some night, with a baseball
 * bat in our hands, than execute an innocent binary that is not to blame.
 */
static int is_osrelease(int min)
{
	int mib[2] = { CTL_KERN, KERN_OSRELEASE };
	char os_release[128];
	size_t len = sizeof(os_release);;

	return sysctl(mib, 2, os_release, &len, NULL, 0) != -1 &&
		atoi(os_release) >= min;
}

static int is_leopard(void)
{
	return is_osrelease(9);
}

static MAYBE_UNUSED int is_tiger(void)
{
	return is_osrelease(8);
}

static int launch_32bit_on_tiger(int argc, char **argv)
{
	const char *match, *replace;

	if (force_32_bit_mode(argv[0]))
		return 0;

	if (is_intel() && is_leopard()) {
		match = "-tiger";
		replace = "-macosx";
	}
	else { /* Tiger */
		match = "-macosx";
		replace = "-tiger";
		if (sizeof(void *) < 8)
			return 0; /* already 32-bit, everything's fine */
	}

	int offset = strlen(argv[0]) - strlen(match);
	if (offset < 0 || strcmp(argv[0] + offset, match))
		return 0; /* suffix not found, no replacement */

	if (strlen(replace) > strlen(match)) {
		char *buffer = (char *)xmalloc(offset + strlen(replace) + 1);
		memcpy(buffer, argv[0], offset);
		argv[0] = buffer;
	}
	strcpy(argv[0] + offset, replace);
	if (!file_exists(argv[0])) {
		strcpy(argv[0] + offset, match);
		return 0;
	}
	hide_splash();
	execv(argv[0], argv);
	fprintf(stderr, "Could not execute %s: %d(%s)\n",
		argv[0], errno, strerror(errno));
	exit(1);
}
#endif

/* check whether there a file is a native library */

static int read_exactly(int fd, unsigned char *buffer, int size)
{
	while (size > 0) {
		int count = read(fd, buffer, size);
		if (count < 0)
			return 0;
		if (count == 0)
			/* short file */
			return 1;
		buffer += count;
		size -= count;
	}
	return 1;
}

/* returns bit-width (32, 64), or 0 if it is not a .dll */
static int MAYBE_UNUSED is_dll(const char *path)
{
	int in;
	unsigned char buffer[0x40];
	unsigned char *p;
	off_t offset;

	if (suffixcmp(path, strlen(path), ".dll"))
		return 0;

	if ((in = open(path, O_RDONLY | O_BINARY)) < 0)
		return 0;

	if (!read_exactly(in, buffer, sizeof(buffer)) ||
			buffer[0] != 'M' || buffer[1] != 'Z') {
		close(in);
		return 0;
	}

	p = (unsigned char *)(buffer + 0x3c);
	offset = p[0] | (p[1] << 8) | (p[2] << 16) | (p[2] << 24);
	lseek(in, offset, SEEK_SET);
	if (!read_exactly(in, buffer, 0x20) ||
			buffer[0] != 'P' || buffer[1] != 'E' ||
			buffer[2] != '\0' || buffer[3] != '\0') {
		close(in);
		return 0;
	}

	close(in);
	if (buffer[0x17] & 0x20)
		return (buffer[0x17] & 0x1) ? 32 : 64;
	return 0;
}

static int MAYBE_UNUSED is_elf(const char *path)
{
	int in;
	unsigned char buffer[0x40];

	if (suffixcmp(path, strlen(path), ".so"))
		return 0;

	if ((in = open(path, O_RDONLY | O_BINARY)) < 0)
		return 0;

	if (!read_exactly(in, buffer, sizeof(buffer))) {
		close(in);
		return 0;
	}

	close(in);
	if (buffer[0] == '\x7f' && buffer[1] == 'E' && buffer[2] == 'L' &&
			buffer[3] == 'F')
		return buffer[4] * 32;
	return 0;
}

static int MAYBE_UNUSED is_dylib(const char *path)
{
	int in;
	unsigned char buffer[0x40];

	if (suffixcmp(path, strlen(path), ".dylib") &&
			suffixcmp(path, strlen(path), ".jnilib"))
		return 0;

	if ((in = open(path, O_RDONLY | O_BINARY)) < 0)
		return 0;

	if (!read_exactly(in, buffer, sizeof(buffer))) {
		close(in);
		return 0;
	}

	close(in);
	if (buffer[0] == 0xca && buffer[1] == 0xfe && buffer[2] == 0xba &&
			buffer[3] == 0xbe && buffer[4] == 0x00 &&
			buffer[5] == 0x00 && buffer[6] == 0x00 &&
			(buffer[7] >= 1 && buffer[7] < 20))
		return 32 | 64; /* might be a fat one, containing both */
	return 0;
}

static int is_native_library(const char *path)
{
#ifdef __APPLE__
	return is_dylib(path);
#else
	return
#ifdef WIN32
		is_dll(path)
#else
		is_elf(path)
#endif
		== sizeof(char *) * 8;
#endif
}

static void find_newest(struct string *relative_path, int max_depth, const char *file, struct string *result)
{
	int len = relative_path->length;
	DIR *directory;
	struct dirent *entry;

	string_add_char(relative_path, '/');

	string_append(relative_path, file);
	if (file_exists(ij_path(relative_path->buffer)) && is_native_library(ij_path(relative_path->buffer))) {
		string_set_length(relative_path, len);
		if (!result->length || file_is_newer(ij_path(relative_path->buffer), ij_path(result->buffer)))
			string_set(result, relative_path->buffer);
	}

	if (max_depth <= 0)
		return;

	string_set_length(relative_path, len);
	directory = opendir(ij_path(relative_path->buffer));
	if (!directory)
		return;
	string_add_char(relative_path, '/');
	while (NULL != (entry = readdir(directory))) {
		if (entry->d_name[0] == '.')
			continue;
		string_append(relative_path, entry->d_name);
		if (dir_exists(ij_path(relative_path->buffer)))
			find_newest(relative_path, max_depth - 1, file, result);
		string_set_length(relative_path, len + 1);
	}
	closedir(directory);
	string_set_length(relative_path, len);
}

static void set_default_library_path(void)
{
	default_library_path =
#if defined(__APPLE__)
		"";
#elif defined(WIN32)
		sizeof(void *) < 8 ? "bin/client/jvm.dll" : "bin/server/jvm.dll";
#else
		sizeof(void *) < 8 ? "lib/i386/client/libjvm.so" : "lib/amd64/server/libjvm.so";
#endif
}

static void adjust_java_home_if_necessary(void)
{
	struct string *result, *buffer, *path;
	const char *prefix = "jre/";
	int depth = 2;

#ifdef __APPLE__
	/* On MacOSX, we look for libjogl.jnilib instead. */
	library_path = "Home/lib/ext/libjogl.jnilib";
	prefix = "";
	depth = 1;
#else
	set_default_library_path();
	library_path = default_library_path;
#endif

	buffer = string_copy("java");
	result = string_init(32);
	path = string_initf("%s%s", prefix, library_path);

	find_newest(buffer, depth, path->buffer, result);
	if (result->length) {
		if (result->buffer[result->length - 1] != '/')
			string_add_char(result, '/');
		string_append(result, prefix);
		relative_java_home = xstrdup(result->buffer);
	}
	else if (*prefix) {
		find_newest(buffer, depth + 1, library_path, buffer);
		if (result->length)
			relative_java_home = xstrdup(result->buffer);
	}
	string_release(buffer);
	string_release(result);
	string_release(path);
}

int main(int argc, char **argv, char **e)
{
	int size;

#if defined(__APPLE__)
	launch_32bit_on_tiger(argc, argv);
#elif defined(WIN32)
	int len;
#ifdef WIN64
	/* work around MinGW64 breakage */
	argc = __argc;
	argv = __argv;
	argv[0] = _pgmptr;
#endif
	len = strlen(argv[0]);
	if (!suffixcmp(argv[0], len, "ImageJ.exe") ||
			!suffixcmp(argv[0], len, "ImageJ"))
		open_win_console();
#endif
	ij_dir = get_ij_dir(argv[0]);
	adjust_java_home_if_necessary();
	main_argv0 = argv[0];
	main_argv = argv;
	main_argc = argc;

	/* save arguments in case we have to try with a smaller heap */
	size = (argc + 1) * sizeof(char *);
	main_argv_backup = (char **)xmalloc(size);
	memcpy(main_argv_backup, main_argv, size);
	main_argc_backup = argc;

	parse_command_line();

	return start_ij();
}
