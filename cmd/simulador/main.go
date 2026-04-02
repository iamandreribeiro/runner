package main

import "fmt"

// version é injetada em build time via -ldflags "-X main.version=<tag>".
var version = "dev"

func main() {
	fmt.Println("simulador v" + version + " — em construção")
}
