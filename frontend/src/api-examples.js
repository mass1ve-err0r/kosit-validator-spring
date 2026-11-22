export const apiLanguages = ['C (libcurl)', 'COBOL', 'Java', 'Objective-C', 'Python', 'Swift']

export const apiExamples = {
    'C (libcurl)': {
        filename: 'validate.c',
        command: 'cc validate.c -lcurl -o validate && ./validate',
        code: `#include <curl/curl.h>
#include <stdio.h>

int main(void) {
    FILE *file = fopen("invoice.xml", "rb");
    if (!file) return 1;

    fseek(file, 0, SEEK_END);
    long size = ftell(file);
    rewind(file);
    if (size < 0) { fclose(file); return 1; }

    CURL *curl = curl_easy_init();
    if (!curl) { fclose(file); return 1; }

    struct curl_slist *headers = NULL;
    headers = curl_slist_append(headers, "Content-Type: application/xml");
    headers = curl_slist_append(headers, "Accept: application/json");

    curl_easy_setopt(curl, CURLOPT_URL, "http://localhost:8090/api/validate");
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_POST, 1L);
    curl_easy_setopt(curl, CURLOPT_READDATA, file);
    curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE_LARGE, (curl_off_t)size);

    CURLcode result = curl_easy_perform(curl);
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);
    fclose(file);
    return result == CURLE_OK ? 0 : 1;
}`
    },
    'COBOL': {
        filename: 'validate.cob',
        command: 'cobc -x -free validate.cob && ./validate',
        code: `identification division.
program-id. validate.
data division.
working-storage section.
01 command-line pic x(512).
procedure division.
    string
        "curl --fail-with-body -sS "
        "-H 'Content-Type: application/xml' "
        "-H 'Accept: application/json' "
        "--data-binary @invoice.xml "
        "http://localhost:8090/api/validate"
        into command-line
    end-string
    call "SYSTEM" using function trim(command-line)
    goback.`
    },
    Java: {
        filename: 'Validate.java',
        command: 'javac Validate.java && java Validate',
        code: `import java.net.URI;
import java.net.http.*;
import java.nio.file.Path;

public class Validate {
    public static void main(String[] args) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:8090/api/validate"))
            .header("Content-Type", "application/xml")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofFile(Path.of("invoice.xml")))
            .build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
    }
}`
    },
    'Objective-C': {
        filename: 'validate.m',
        command: 'clang -fobjc-arc -framework Foundation validate.m -o validate && ./validate',
        code: `#import <Foundation/Foundation.h>

int main(void) {
    @autoreleasepool {
        NSURL *url = [NSURL URLWithString:@"http://localhost:8090/api/validate"];
        NSData *xml = [NSData dataWithContentsOfFile:@"invoice.xml"];
        if (!xml) return 1;

        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
        request.HTTPMethod = @"POST";
        request.HTTPBody = xml;
        [request setValue:@"application/xml" forHTTPHeaderField:@"Content-Type"];
        [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];

        dispatch_semaphore_t done = dispatch_semaphore_create(0);
        [[[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:
            ^(NSData *data, NSURLResponse *response, NSError *error) {
                if (error) NSLog(@"%@", error);
                else NSLog(@"%@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
                dispatch_semaphore_signal(done);
            }] resume];
        dispatch_semaphore_wait(done, DISPATCH_TIME_FOREVER);
    }
    return 0;
}`
    },
    Python: {
        filename: 'validate.py',
        command: 'python3 validate.py',
        code: `from pathlib import Path
from urllib.request import Request, urlopen

request = Request(
    "http://localhost:8090/api/validate",
    data=Path("invoice.xml").read_bytes(),
    headers={"Content-Type": "application/xml", "Accept": "application/json"},
    method="POST",
)

with urlopen(request) as response:
    print(response.read().decode("utf-8"))`
    },
    Swift: {
        filename: 'Validate.swift',
        command: 'swiftc -parse-as-library Validate.swift -o validate && ./validate',
        code: `import Foundation

@main
struct Validate {
    static func main() async throws {
        var request = URLRequest(url: URL(string: "http://localhost:8090/api/validate")!)
        request.httpMethod = "POST"
        request.setValue("application/xml", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.httpBody = try Data(contentsOf: URL(fileURLWithPath: "invoice.xml"))

        let (data, _) = try await URLSession.shared.data(for: request)
        print(String(decoding: data, as: UTF8.self))
    }
}`
    }
}
