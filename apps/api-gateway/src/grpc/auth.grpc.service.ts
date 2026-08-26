import { Inject, Injectable } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { firstValueFrom, Observable } from 'rxjs';

interface AuthServiceClient {
  signup(data: {
    email: string;
    password: string;
  }): Observable<{
    success: boolean;
    message: string;
  }>;

  login(data: {
    email: string;
    password: string;
  }): Observable<{
    success: boolean;
    message: string;
    sessionToken: string;
  }>;
}

@Injectable()
export class AuthGrpcService {
  private authService: AuthServiceClient;

  constructor(
    @Inject('AUTH_GRPC')
    private readonly client: ClientGrpc,
  ) {}

  onModuleInit() {
    this.authService =
      this.client.getService<AuthServiceClient>('AuthService');
  }

  async signup(email: string, password: string) {
    return firstValueFrom(
      this.authService.signup({
        email,
        password,
      }),
    );
  }

  async login(email: string, password: string) {
    const response = await firstValueFrom(
      this.authService.login({
        email,
        password,
      }),
    );

    console.log('🔥 RAW LOGIN RESPONSE:', response);

    return {
      success: response.success,
      message: response.message,
      sessionToken: response.sessionToken,
    };
  }
}
