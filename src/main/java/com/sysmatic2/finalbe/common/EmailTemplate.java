package com.sysmatic2.finalbe.common;

public class EmailTemplate {

    private EmailTemplate() {
        // Prevent instantiation
    }

    /**
     * Generates the HTML template for email verification.
     *
     * @param verificationCode The verification code to include in the email
     * @return HTML content as a String
     */
    public static String getVerificationHtmlTemplate(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>이메일 인증</title>
              </head>
              <body
                style="
                  margin: 0;
                  padding: 0;
                  background-color: #f4f4f5;
                  font-family: 'Malgun Gothic', '맑은 고딕', Arial, sans-serif;
                "
              >
                <table
                  role="presentation"
                  cellpadding="0"
                  cellspacing="0"
                  style="
                    width: 600px;
                    height: 700px;
                    margin: 100px auto;
                    background-color: #ffffff;
                    border-radius: 8px;
                  "
                >
                  <tr>
                    <td style="display: block; padding: 40px">
                      <div>
                        <img src="https://fastcampus-team2.s3.ap-northeast-2.amazonaws.com/admin/icon/7745bc6a-3837-4c00-a5b4-5264c047cc00.png" alt="로고" height="50" style="display: block" />
                      </div>
                      <h1
                        style="
                          color: #0d9488;
                          font-size: 34px;
                          font-weight: 700;
                          line-height: 140%;
                          margin: 140px 0 10px 0;
                          text-align: center;
                        "
                      >
                        인증번호
                      </h1>
                      <div style="background-color: #f0fdfa; padding: 24px; margin: 0 0 30px 0">
                        <p
                          style="
                            color: #18181b;
                            font-size: 60px;
                            font-weight: 700;
                            line-height: 130%;
                            letter-spacing: 2px;
                            margin: 0;
                            text-align: center;
                          "
                        >
                          """ + verificationCode + """
                        </p>
                      </div>
                      <p
                        style="
                          color: #52525b;
                          font-size: 16px;
                          font-weight: 500;
                          line-height: 150%;
                          margin: 32px 0 0;
                        "
                      >
                        시스메틱 가입을 위한 인증번호입니다.<br />
                        위 인증번호 6자리를 입력하여 이메일 주소 인증을 완료해 주세요
                      </p>
                    </td>
                  </tr>
                </table>
              </body>
            </html>
        """;
    }

}
