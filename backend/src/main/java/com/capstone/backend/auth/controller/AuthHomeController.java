package com.capstone.backend.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class AuthHomeController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Login Success</title>
                  <style>
                    body {
                      margin: 0;
                      font-family: system-ui, sans-serif;
                      background: #f4f7fb;
                      color: #17202a;
                    }
                    .wrap {
                      max-width: 720px;
                      margin: 40px auto;
                      padding: 24px;
                    }
                    .card {
                      background: #fff;
                      border-radius: 16px;
                      padding: 24px;
                      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
                    }
                    h1 {
                      margin-top: 0;
                    }
                    .item {
                      margin: 12px 0;
                      padding: 12px;
                      background: #f8fafc;
                      border-radius: 10px;
                      word-break: break-all;
                    }
                    .label {
                      font-size: 12px;
                      color: #5b6570;
                      margin-bottom: 6px;
                    }
                    code {
                      font-size: 14px;
                    }
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <div class="card">
                      <h1>소셜 로그인 결과</h1>
                      <p>리다이렉트로 전달된 값을 아래에서 확인할 수 있습니다.</p>
                      <div class="item">
                        <div class="label">provider</div>
                        <code id="provider"></code>
                      </div>
                      <div class="item">
                        <div class="label">providerId</div>
                        <code id="providerId"></code>
                      </div>
                      <div class="item">
                        <div class="label">email</div>
                        <code id="email"></code>
                      </div>
                      <div class="item">
                        <div class="label">name</div>
                        <code id="name"></code>
                      </div>
                      <div class="item">
                        <div class="label">code</div>
                        <code id="code"></code>
                      </div>
                      <div class="item">
                        <div class="label">signupRequired</div>
                        <code id="signupRequired"></code>
                      </div>
                      <div class="item">
                        <div class="label">signupCode</div>
                        <code id="signupCode"></code>
                      </div>
                    </div>
                  </div>
                  <script>
                    const params = new URLSearchParams(window.location.search);
                    ["provider", "providerId", "email", "name", "code", "signupRequired", "signupCode"].forEach((key) => {
                      const element = document.getElementById(key);
                      if (element) {
                        element.textContent = params.get(key) ?? "";
                      }
                    });
                  </script>
                </body>
                </html>
                """;
    }
}
