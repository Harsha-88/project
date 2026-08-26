import { Args, Mutation, Query, Resolver, ObjectType, Field } from '@nestjs/graphql';
import { AuthGrpcService } from '../grpc/auth.grpc.service';

@ObjectType()
class SignupResponse {
  @Field()
  success: boolean;

  @Field()
  message: string;
}

@ObjectType()
class LoginResponse {
  @Field()
  success: boolean;

  @Field()
  message: string;

  @Field()
  sessionToken: string;
}

@Resolver()
export class AppResolver {
  constructor(
    private readonly authGrpcService: AuthGrpcService,
  ) {}

  @Query(() => String)
  hello(): string {
    return 'Hello from API Gateway';
  }

  @Mutation(() => SignupResponse)
  async signup(
    @Args('email') email: string,
    @Args('password') password: string,
  ): Promise<SignupResponse> {
    return this.authGrpcService.signup(email, password);
  }

  @Mutation(() => LoginResponse)
  async login(
    @Args('email') email: string,
    @Args('password') password: string,
  ): Promise<LoginResponse> {
    const response = await this.authGrpcService.login(email, password);

    return {
      success: response.success,
      message: response.message,
      sessionToken: response.sessionToken,
    };
  }
}
