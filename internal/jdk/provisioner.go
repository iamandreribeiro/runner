package jdk

import (
	"archive/tar"
	"archive/zip"
	"compress/gzip"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
)

// adoptiumURLTemplate is the Adoptium API endpoint for the latest JDK 21 GA binary.
// Parameters: os (linux|windows|mac), arch (x64|aarch64), imageType (jdk).
const adoptiumURLTemplate = "https://api.adoptium.net/v3/binary/latest/21/ga/%s/%s/jdk/hotspot/normal/eclipse"

// provision downloads a JDK 21 from Adoptium into CacheDir and returns
// the path to the java executable.
func provision() (string, error) {
	cacheDir, err := CacheDir()
	if err != nil {
		return "", err
	}
	if err := os.MkdirAll(cacheDir, 0o755); err != nil {
		return "", fmt.Errorf("não foi possível criar o diretório de cache do JDK: %w", err)
	}

	os_, arch := adoptiumPlatform()
	url := fmt.Sprintf(adoptiumURLTemplate, os_, arch)

	fmt.Fprintf(os.Stderr, "JDK 21 não encontrado. Baixando de %s ...\n", url)

	archive, err := download(url, cacheDir)
	if err != nil {
		return "", fmt.Errorf("falha ao baixar o JDK: %w", err)
	}
	defer os.Remove(archive)

	extractDir := cacheDir
	if err := extract(archive, extractDir); err != nil {
		return "", fmt.Errorf("falha ao extrair o JDK: %w", err)
	}

	java := detectFromCache()
	if java == "" {
		return "", fmt.Errorf("JDK extraído mas executável java não localizado em %s", cacheDir)
	}

	fmt.Fprintf(os.Stderr, "JDK 21 instalado em: %s\n", java)
	return java, nil
}

// download fetches url into a temp file inside dir and returns the path.
func download(url, dir string) (string, error) {
	resp, err := http.Get(url) //nolint:gosec // URL is constructed from trusted constants + runtime.GOOS/GOARCH
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("HTTP %d ao baixar %s", resp.StatusCode, url)
	}

	tmp, err := os.CreateTemp(dir, "jdk-download-*")
	if err != nil {
		return "", err
	}
	defer tmp.Close()

	if _, err := io.Copy(tmp, resp.Body); err != nil {
		os.Remove(tmp.Name())
		return "", err
	}
	return tmp.Name(), nil
}

// extract unpacks a .zip or .tar.gz archive into destDir.
func extract(archivePath, destDir string) error {
	if isWindows() {
		return extractZip(archivePath, destDir)
	}
	return extractTarGz(archivePath, destDir)
}

func extractTarGz(src, dest string) error {
	f, err := os.Open(src)
	if err != nil {
		return err
	}
	defer f.Close()

	gz, err := gzip.NewReader(f)
	if err != nil {
		return err
	}
	defer gz.Close()

	tr := tar.NewReader(gz)
	for {
		hdr, err := tr.Next()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}
		target := filepath.Join(dest, filepath.FromSlash(hdr.Name)) //nolint:gosec
		switch hdr.Typeflag {
		case tar.TypeDir:
			if err := os.MkdirAll(target, 0o755); err != nil {
				return err
			}
		case tar.TypeReg:
			if err := writeFile(target, tr, hdr.FileInfo().Mode()); err != nil {
				return err
			}
		case tar.TypeSymlink:
			_ = os.Symlink(hdr.Linkname, target)
		}
	}
	return nil
}

func extractZip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}
	defer r.Close()

	for _, f := range r.File {
		target := filepath.Join(dest, filepath.FromSlash(f.Name)) //nolint:gosec
		if f.FileInfo().IsDir() {
			if err := os.MkdirAll(target, 0o755); err != nil {
				return err
			}
			continue
		}
		rc, err := f.Open()
		if err != nil {
			return err
		}
		if err := writeFile(target, rc, f.Mode()); err != nil {
			rc.Close()
			return err
		}
		rc.Close()
	}
	return nil
}

func writeFile(path string, r io.Reader, mode os.FileMode) error {
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return err
	}
	out, err := os.OpenFile(path, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, mode)
	if err != nil {
		return err
	}
	defer out.Close()
	_, err = io.Copy(out, r)
	return err
}

// adoptiumPlatform maps runtime.GOOS / runtime.GOARCH to Adoptium API values.
func adoptiumPlatform() (os_, arch string) {
	switch runtime.GOOS {
	case "windows":
		os_ = "windows"
	case "darwin":
		os_ = "mac"
	default:
		os_ = "linux"
	}
	switch runtime.GOARCH {
	case "arm64":
		arch = "aarch64"
	default:
		arch = "x64"
	}
	return
}

func isWindows() bool {
	return runtime.GOOS == "windows"
}
