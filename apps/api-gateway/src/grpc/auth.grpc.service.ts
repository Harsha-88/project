import {
  Inject,
  Injectable,
  OnModuleInit,
} from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { firstValueFrom, Observable } from 'rxjs';

interface AuthRequest {
  email: string;
  password: string;
}

interface SignupResponse {
  success: boolean;
  message: string;
}

interface LoginResponse {
  success: boolean;
  message: string;
  sessionToken: string;
}

interface AuthServiceClient {
  signup(data: AuthRequest): Observable<SignupResponse>;
  login(data: AuthRequest): Observable<LoginResponse>;
}

@Injectable()
export class AuthGrpcService implements OnModuleInit {
  private authService!: AuthServiceClient;

  constructor(
    @Inject('AUTH_GRPC')
    private readonly client: ClientGrpc,
  ) {}

  onModuleInit(): void {
    this.authService =
      this.client.getService<AuthServiceClient>('AuthService');
  }

  async signup(
    email: string,
    password: string,
  ): Promise<SignupResponse> {
    return firstValueFrom(
      this.authService.signup({
        email,
        password,
      }),
    );
  }

  async login(
    email: string,
    password: string,
  ): Promise<LoginResponse> {
    return firstValueFrom(
      this.authService.login({
        email,
        password,
      }),
    );
  }
}
