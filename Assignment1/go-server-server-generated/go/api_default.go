/*
 * Album Store API
 *
 * CS6650 Fall 2023
 *
 * API version: 1.0.0
 * Contact: i.gorton@northeasern.edu
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */
package swagger

import (
	"encoding/json"
	"github.com/gorilla/mux"
	"net/http"
	"strings"
)

// AlbumResponse represents the JSON response body
type AlbumResponse struct {
	AlbumID   string `json:"albumId"`
	ImageSize int    `json:"imageSize"`
}

func GetAlbumByKey(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=UTF-8")

	vars := mux.Vars(r)
	albumID, ok := vars["albumID"]

	// Check if the albumID is provided in the URL
	if !ok || albumID == "" {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte(`{"error": "Missing parameters"}`))
		return
	}

	// If the albumID is valid, return a constant JSON response
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(`{"albumID": "123", "title": "Example Title", "year": "Example Year"}`))
}

func NewAlbum(w http.ResponseWriter, r *http.Request) {
	// Check if it is a multipart request
	if !strings.HasPrefix(r.Header.Get("Content-Type"), "multipart/form-data;") {
		http.Error(w, "Error: not a multipart request", http.StatusBadRequest)
		return
	}

	// Parse the multipart form
	err := r.ParseMultipartForm(10 << 20) // 10 MB limit
	if err != nil {
		http.Error(w, "Unable to parse form", http.StatusInternalServerError)
		return
	}

	// Assuming "image" is the name attribute in your form
	_, _, err = r.FormFile("image")
	if err != nil {
		http.Error(w, "Unable to get the file", http.StatusInternalServerError)
		return
	}

	// Assuming "profile" is the name attribute in your form for a regular input field
	profile := r.FormValue("profile")
	if profile == "" {
		http.Error(w, "Profile is missing", http.StatusBadRequest)
		return
	}

	// Creating a response with fixed album ID and other necessary data
	response := AlbumResponse{
		AlbumID:   "123",
		ImageSize: 10, // You should calculate the actual image size
	}

	// Setting content type as JSON
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)

	// Encoding the response into JSON
	err = json.NewEncoder(w).Encode(response)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}
